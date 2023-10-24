package me.pr3.api.ui;

import me.pr3.api.types.ShikakuGame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author tim
 */
public class ShikakuGamePainter extends JFrame {

    ShikakuGame game = null;

    private Image dbImage;
    private Graphics dbg;
    JPanel contentPane = new JPanel();

    public ShikakuGamePainter() {


        setSize(1920, 1080);
        setVisible(true);

        contentPane = new JPanel();
        contentPane.setBackground(Color.LIGHT_GRAY);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        setContentPane(contentPane);

    }

    public void preparePaint(ShikakuGame game) {
        this.game = game;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (dbImage == null) {
            dbImage = createImage(this.getSize().width, this.getSize().height);
            dbg = dbImage.getGraphics();
        }

        dbg.clearRect(0,0, 1920, 1080);

        dbg.setColor(Color.BLACK);
        for (int x = 0; x < game.bounds.width; x++) {
            for (int y = 0; y < game.bounds.height; y++) {
                dbg.drawRect(x * 20, y * 20, 20, 20);
            }
        }

        for (Rectangle rectangle : game.rectangles) {
            dbg.setColor(new Color(rectangle.hashCode() % 0xFFFFFF));
            dbg.fillRect(rectangle.x * 20, rectangle.y * 20, rectangle.width * 20, rectangle.height * 20);
        }

        game.numbers.forEach((point, integer) -> {
            dbg.setColor(Color.BLACK);
            dbg.setFont(dbg.getFont().deriveFont(10f));
            dbg.drawString(integer.toString(), point.x * 20, point.y * 20 + 20);
        });
        g.drawImage(dbImage, 8, 32, null);
    }
}
