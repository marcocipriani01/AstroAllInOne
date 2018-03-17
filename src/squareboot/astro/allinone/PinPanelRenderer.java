package squareboot.astro.allinone;

import javax.swing.*;
import java.awt.*;

/**
 * Custom renderer for {@link AbstractPinPanel}.
 *
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class PinPanelRenderer implements ListCellRenderer<AbstractPinPanel> {

    /**
     * Class constructor.
     */
    public PinPanelRenderer() {

    }

    @Override
    public Component getListCellRendererComponent(JList<? extends AbstractPinPanel> list, AbstractPinPanel value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        JPanel panel = value.getPanel();
        if (isSelected) {
            panel.setBackground(list.getSelectionBackground());
            panel.setForeground(list.getSelectionForeground());

        } else {
            panel.setBackground(list.getBackground());
            panel.setForeground(list.getForeground());
        }
        return panel;
    }
}