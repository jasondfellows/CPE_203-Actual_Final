import processing.core.PApplet;
import processing.core.PImage;

import java.util.Optional;
import java.util.Scanner;

public final class WorldView
{
    private PApplet screen;
    private WorldModel world;
    private int tileWidth;
    private int tileHeight;
    public Viewport viewport;

    public WorldView(
            int numRows,
            int numCols,
            PApplet screen,
            WorldModel world,
            int tileWidth,
            int tileHeight)
    {
        this.screen = screen;
        this.world = world;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.viewport = new Viewport(numRows, numCols);
    }
    public static void drawViewport(WorldView view) {
        view.drawBackground(view);
        view.drawEntities(view);
    }
    public void drawEntities(WorldView view) {
        for (Entity entity : view.world.entities) {
            Point pos = entity.getPosition();

            if (view.viewport.contains(view.viewport, pos)) {
                Point viewPoint = view.viewport.worldToViewport(view.viewport, (int)pos.x, (int)pos.y);
                view.screen.image(Background.getCurrentImage(entity),
                        (int)viewPoint.x * view.tileWidth,
                        (int)viewPoint.y * view.tileHeight);
            }
        }
    }


    public void shiftView(WorldView view, int colDelta, int rowDelta) {
        int newCol = Functions.clamp(view.viewport.col + colDelta, 0,
                view.world.numCols - view.viewport.numCols);
        int newRow = Functions.clamp(view.viewport.row + rowDelta, 0,
                view.world.numRows - view.viewport.numRows);

        view.viewport.shift(view.viewport, newCol, newRow);
    }

    public void drawBackground(WorldView view) {
        for (int row = 0; row < view.viewport.numRows; row++) {
            for (int col = 0; col < view.viewport.numCols; col++) {
                Point worldPoint = view.viewport.viewportToWorld(view.viewport, col, row);
                Optional<PImage> image =
                        Entity.getBackgroundImage(view.world, worldPoint);
                if (image.isPresent()) {
                    view.screen.image(image.get(), col * view.tileWidth,
                            row * view.tileHeight);
                }
            }
        }
    }
}
