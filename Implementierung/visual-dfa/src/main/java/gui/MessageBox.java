package gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * DialogBox for displaying messages. Can display a custom message.
 * 
 * @author Michael
 *
 * @see DialogBox
 */

public class MessageBox extends DialogBox {

    
    private JTextArea messageArea;
    private String message;
    
    /**
     * Display the MessageBox. Stop execution of this Thread until user closes
     * the Dialog.
     * 
     * @param owner
     *            The Frame, which is the owner of the MessageBox.
     * @param title
     *            The title of the MessageBox.
     * @param message
     *            The message to be displayed by the MessageBox.
     * 
     * @see Frame
     * @see javax.swing.JDialog
     */
    public MessageBox(Frame owner, String title, String message) {
        super(owner);
        this.message = message;
        init(title);
        pack();
        setVisible(true);
    }

    /**
     * Set layout and content of the contentPanel for the MessageBox.
     */
    @Override
    protected void initContentPanel() {
        contentPanel.setBackground(Colors.BACKGROUND.getColor());
        
        messageArea = new JTextArea();
        int i = message.length();
        //This makes boxes with more text larger than boxes with fewer text
        if ((i / 25) > 4) {
            messageArea.setColumns(30);
            messageArea.setRows(15);
        } else {
            messageArea.setColumns(25);
            messageArea.setRows(4);
        }
     
        new JComponentDecorator().decorate(messageArea);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setText(message);
        
        JScrollPane messagePane = new JScrollPane(messageArea);
        messagePane.setBorder(null);
        contentPanel.add(messagePane);
    }

    /**
     * Set layout and content of the ButtonPane for the MessageBox.
     */
    @Override
    protected void initButtonPane() {
        
        buttonPane.setBackground(Colors.BACKGROUND.getColor());
        GridBagLayout gbl_Button = new GridBagLayout();
        gbl_Button.columnWidths = new int[] {0, 0, 0};
        gbl_Button.columnWeights = new double[] {0.5, 0.5, 0.5};
        buttonPane.setLayout(gbl_Button);
        JButtonDecorator buttonDecorator = new JButtonDecorator(new JComponentDecorator());
        JButton btnOK = new JButton();
        buttonDecorator.decorateBorderButton(btnOK, new OKListener(), "OK", Colors.GREY_BORDER.getColor());
        btnOK.setBackground(Colors.WHITE_BACKGROUND.getColor());
        btnOK.setForeground(Colors.DARK_TEXT.getColor());
        GridBagConstraints gbc_btnOK = GridBagConstraintFactory.getStandardGridBagConstraints(1, 0, 1, 1);
        buttonPane.add(btnOK, gbc_btnOK);
        

    }
    
    private class OKListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
            
        }
        
    }

}
