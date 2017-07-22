package gui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

/**
 * Utility class to set standard properties for jComponents.
 * 
 * @author Michael
 *
 */
public class JComponentDecorator {
    
    /**
     * Set standard properties for the given jComponent.
     * 
     * @param comp Set properties on this component.
     * @see JComponent
     */
    public void decorate(JComponent comp) {
        comp.setBackground(new Color(0, 0, 102));
        comp.setBorder(new EmptyBorder(2, 2, 2, 2));
        comp.setForeground(new Color(255, 255, 255));
        comp.setFont(new Font("Trebuchet MS", Font.BOLD, 16));
    }
    
    
}