package paint.strategy;

import paint.app.FillStyle;
import paint.app.PaintModel;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 * DrawingStrategy interface handles the drawing process of drawable shapes.
 * Using MouseEvent to receive the mouse interaction to draw the drawable shapes
 * based on the mouse interactions.
 */
public interface DrawingStrategy {
    /**
     * Handles the drawing process for drawing drawable shapes.
     * @param e
     * @param model
     * @param color
     * @param lineWidth
     * @param fillStyle
     */
    void handle(MouseEvent e, PaintModel model, Color color, double lineWidth, FillStyle fillStyle);
}
