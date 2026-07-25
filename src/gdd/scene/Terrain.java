package gdd.scene;

import static gdd.Global.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Scrolling cave walls along the top and bottom of the playfield.
 *
 * The strip is made of TERRAIN_TILE-px-wide columns; each column stores how
 * many rock tiles hang from the top and rise from the bottom (dashboard edge).
 * Everything scrolls right -> left one pixel per frame — the same clock the
 * starfield uses.
 *
 * Columns come from a hand-editable map file (see src/maps/level1.txt for the
 * format). Past the end of an authored map the last column simply repeats, so
 * a map that is shorter than its stage never crashes — it just goes flat. If
 * the file is missing or unreadable the old procedural generator takes over (a
 * random walk with plateaus) so the game still runs.
 */
public class Terrain {

    private static final int TILE = TERRAIN_TILE;

    // Gray rock with a darker crosshatch texture, plus green plant tufts —
    // matches the cave tileset the walls are modeled on.
    private static final Color ROCK_BASE = new Color(118, 118, 128);
    private static final Color ROCK_HATCH = new Color(72, 72, 82);
    private static final Color ROCK_EDGE = new Color(50, 50, 58);
    private static final Color PLANT_DARK = new Color(0, 150, 40);
    private static final Color PLANT_LIGHT = new Color(60, 230, 70);

    // Playfield height in tiles (dashboard excluded).
    private final int fieldTiles = (BOARD_HEIGHT - DASHBOARD_HEIGHT) / TILE;
    // Tightest corridor this map may reach. Authored maps override it via
    // their "minGap" header so level 2 can squeeze past the global default.
    private int minGapTiles = TERRAIN_MIN_GAP / TILE;

    // col[0] = tiles hanging from the top, col[1] = tiles rising from the bottom.
    private final List<int[]> columns = new ArrayList<>();
    private final Random rng;
    private final long plantSeed;
    // True once a map file loaded: past the end we hold the last column
    // instead of resuming the random walk.
    private boolean authored = false;

    /**
     * Loads mapPath; falls back to procedural generation seeded with
     * fallbackSeed if the file is missing or unusable.
     */
    public Terrain(String mapPath, long fallbackSeed) {
        rng = new Random(fallbackSeed);
        plantSeed = fallbackSeed;
        if (!load(mapPath)) {
            // Gentle opening stretch so the player's spawn area is always safe.
            for (int i = 0; i < 30; i++) {
                columns.add(new int[]{1, 1});
            }
        }
    }

    /** Purely procedural terrain — no map file. */
    public Terrain(long seed) {
        this(null, seed);
    }

    /** Authored column count, or 0 when running procedurally. */
    public int length() {
        return authored ? columns.size() : 0;
    }

    // Column by absolute index, generating any missing ones on the way.
    private int[] column(int c) {
        if (c < 0) {
            c = 0;
        }
        if (authored) {
            // Hold the final column once the authored map runs out.
            return columns.get(Math.min(c, columns.size() - 1));
        }
        while (columns.size() <= c) {
            columns.add(nextColumn(columns.get(columns.size() - 1)));
        }
        return columns.get(c);
    }

    /**
     * Reads a map file. Data lines are "top bottom [count]"; a line starting
     * with a non-numeric token is a "key value" header (only minGap so far).
     * Blank lines and everything from a '#' onward are ignored.
     *
     * Bad columns are repaired rather than fatal — a typo in the map should
     * cost you a warning on stderr, not the whole run.
     */
    private boolean load(String path) {
        if (path == null) {
            return false;
        }
        List<int[]> loaded = new ArrayList<>();
        int gapFloor = minGapTiles;
        int lineNo = 0;
        try (BufferedReader r = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = r.readLine()) != null) {
                lineNo++;
                int hash = line.indexOf('#');
                if (hash >= 0) {
                    line = line.substring(0, hash);
                }
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\s+");
                if (!parts[0].matches("-?\\d+")) {
                    if (parts.length >= 2 && parts[0].equals("minGap")) {
                        gapFloor = Integer.parseInt(parts[1]);
                    } else {
                        System.err.println(path + ":" + lineNo
                                + ": unknown header '" + parts[0] + "', ignored");
                    }
                    continue;
                }
                if (parts.length < 2) {
                    System.err.println(path + ":" + lineNo
                            + ": expected 'top bottom [count]', ignored");
                    continue;
                }
                int top = Integer.parseInt(parts[0]);
                int bottom = Integer.parseInt(parts[1]);
                int count = parts.length > 2 ? Integer.parseInt(parts[2]) : 1;
                for (int i = 0; i < count; i++) {
                    loaded.add(repair(top, bottom, gapFloor, path, lineNo));
                }
            }
        } catch (Exception e) {
            System.err.println("Terrain: could not load " + path + " ("
                    + e.getMessage() + "), using procedural walls");
            return false;
        }
        if (loaded.isEmpty()) {
            System.err.println("Terrain: " + path
                    + " has no columns, using procedural walls");
            return false;
        }
        columns.addAll(loaded);
        minGapTiles = gapFloor;
        authored = true;
        return true;
    }

    // Clamp one authored column into a legal, flyable shape.
    private int[] repair(int top, int bottom, int gapFloor, String path, int lineNo) {
        int t = Math.max(1, Math.min(TERRAIN_MAX_WALL_TILES, top));
        int b = Math.max(1, Math.min(TERRAIN_MAX_WALL_TILES, bottom));
        while (fieldTiles - t - b < gapFloor && (t > 1 || b > 1)) {
            if (t >= b) {
                t--;
            } else {
                b--;
            }
        }
        if (t != top || b != bottom) {
            System.err.println(path + ":" + lineNo + ": column " + top + " " + bottom
                    + " widened to " + t + " " + b + " (minGap " + gapFloor + ")");
        }
        return new int[]{t, b};
    }

    private int[] nextColumn(int[] prev) {
        int top = step(prev[0]);
        int bottom = step(prev[1]);
        // Keep the corridor flyable.
        while (top + bottom > fieldTiles - minGapTiles) {
            if (top >= bottom) {
                top--;
            } else {
                bottom--;
            }
        }
        return new int[]{top, bottom};
    }

    // Random walk: hold half the time (plateaus), otherwise step one tile.
    private int step(int h) {
        int r = rng.nextInt(4);
        if (r == 2) {
            h--;
        } else if (r == 3) {
            h++;
        }
        return Math.max(1, Math.min(TERRAIN_MAX_WALL_TILES, h));
    }

    public void draw(Graphics g, int scroll, int panelW, int fieldBottom) {
        int firstCol = scroll / TILE;
        int offset = scroll % TILE;
        int cols = panelW / TILE + 2; // +2 for smooth scrolling

        for (int sc = 0; sc < cols; sc++) {
            int c = firstCol + sc;
            int[] col = column(c);
            int x = sc * TILE - offset;

            for (int t = 0; t < col[0]; t++) {
                drawTile(g, x, t * TILE);
            }
            for (int t = 1; t <= col[1]; t++) {
                drawTile(g, x, fieldBottom - t * TILE);
            }

            // Darker lip along each corridor-facing surface.
            g.setColor(ROCK_EDGE);
            g.fillRect(x, col[0] * TILE - 2, TILE, 2);
            g.fillRect(x, fieldBottom - col[1] * TILE, TILE, 2);

            // Plants are deterministic per column so they don't flicker.
            Random pr = new Random(plantSeed ^ (c * 0x9E3779B97F4A7C15L));
            if (pr.nextInt(4) == 0) {
                drawPlant(g, x, fieldBottom - col[1] * TILE, -1, pr);
            }
            if (pr.nextInt(5) == 0) {
                drawPlant(g, x, col[0] * TILE, 1, pr);
            }
        }
    }

    private void drawTile(Graphics g, int x, int y) {
        g.setColor(ROCK_BASE);
        g.fillRect(x, y, TILE, TILE);
        // Crosshatch of small X marks, offset every other row.
        g.setColor(ROCK_HATCH);
        for (int py = 4; py <= TILE - 4; py += 8) {
            int shift = ((y + py) / 8 % 2 == 0) ? 0 : 4;
            for (int px = 4 + shift; px <= TILE - 4; px += 8) {
                g.drawLine(x + px - 2, y + py - 2, x + px + 2, y + py + 2);
                g.drawLine(x + px + 2, y + py - 2, x + px - 2, y + py + 2);
            }
        }
    }

    // Wavy seaweed-style strands: dir -1 grows up off the bottom wall,
    // dir +1 hangs down from the top wall.
    private void drawPlant(Graphics g, int x, int surfaceY, int dir, Random pr) {
        int strands = 3 + pr.nextInt(3);
        for (int s = 0; s < strands; s++) {
            g.setColor(s % 2 == 0 ? PLANT_LIGHT : PLANT_DARK);
            int bx = x + 6 + pr.nextInt(TILE - 12);
            int len = 16 + pr.nextInt(20);
            int wob = pr.nextBoolean() ? 2 : -2;
            int px = bx;
            int py = surfaceY;
            for (int seg = 4; seg <= len; seg += 4) {
                int nx = bx + (((seg / 4) % 2 == 0) ? wob : -wob);
                int ny = surfaceY + dir * seg;
                g.drawLine(px, py, nx, ny);
                px = nx;
                py = ny;
            }
        }
    }

    // Does the (screen-space) rect overlap any wall right now?
    public boolean collides(Rectangle r, int scroll, int fieldBottom) {
        int firstCol = Math.floorDiv(r.x + scroll, TILE);
        int lastCol = Math.floorDiv(r.x + r.width - 1 + scroll, TILE);
        for (int c = firstCol; c <= lastCol; c++) {
            int[] col = column(c);
            int topPx = col[0] * TILE;
            int bottomWallTop = fieldBottom - col[1] * TILE;
            if (r.y < topPx || r.y + r.height > bottomWallTop) {
                return true;
            }
        }
        return false;
    }

    // Clamp a sprite's y so it sits inside the open corridor at screenX.
    public int clampToGap(int screenX, int scroll, int desiredY, int spriteH, int fieldBottom) {
        int[] col = column(Math.floorDiv(screenX + scroll, TILE));
        int top = col[0] * TILE + 8;
        int bottom = fieldBottom - col[1] * TILE - 8 - spriteH;
        return Math.max(top, Math.min(desiredY, bottom));
    }
}
