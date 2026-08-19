package com.banking.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;


public final class UIStyle {

    public static final Color PRIMARY_DARK = new Color(0x0D, 0x47, 0xA1);
    public static final Color PRIMARY = new Color(0x15, 0x65, 0xC0);
    public static final Color LIGHT_BG = new Color(0xE3, 0xF2, 0xFD);
    public static final Color WHITE = Color.WHITE;
    public static final Color TEXT_DARK = new Color(0x21, 0x21, 0x21);
    public static final Color SUCCESS = new Color(0x2E, 0x7D, 0x32);
    public static final Color DANGER = new Color(0xC6, 0x28, 0x28);
    public static final Color PURPLE = new Color(0x6A, 0x1B, 0x9A);
    public static final Color GOLD = new Color(0xF9, 0xA8, 0x25);
    public static final Color BORDER = new Color(0xB0, 0xBE, 0xC5);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 16);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_FIELD = new Font("Segoe UI", Font.PLAIN, 18);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_BIG_NUMBER = new Font("Segoe UI", Font.BOLD, 34);
    public static final Font FONT_LINK = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 14);

    private UIStyle() {
    }

    public static TextField textField(int columns) {
        TextField tf = new TextField(columns);
        tf.setFont(FONT_FIELD);
        tf.setForeground(TEXT_DARK);
        tf.setBackground(WHITE);
        tf.setPreferredSize(new Dimension(280, 40));
        return tf;
    }

    public static TextField pinField(int columns) {
        TextField tf = textField(columns);
        tf.setEchoChar('*');
        return tf;
    }

    public static Label label(String text) {
        Label l = new Label(text);
        l.setFont(FONT_LABEL);
        l.setForeground(TEXT_DARK);
        return l;
    }

    public static Label heading(String text, Color color, int size) {
        Label l = new Label(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, size));
        l.setForeground(color);
        return l;
    }

    public static Button primaryButton(String text) {
        Button b = new Button(text);
        b.setFont(FONT_BUTTON);
        b.setBackground(PRIMARY);
        b.setForeground(WHITE);
        b.setPreferredSize(new Dimension(160, 46));
        return b;
    }

    public static Button successButton(String text) {
        Button b = primaryButton(text);
        b.setBackground(SUCCESS);
        return b;
    }

    public static Button dangerButton(String text) {
        Button b = primaryButton(text);
        b.setBackground(DANGER);
        return b;
    }

    public static Button purpleButton(String text) {
        Button b = primaryButton(text);
        b.setBackground(PURPLE);
        return b;
    }

    public static Button neutralButton(String text) {
        Button b = primaryButton(text);
        b.setBackground(new Color(0x78, 0x90, 0x9C));
        return b;
    }

    /** Looks like a hyperlink but is a real Button — clicking it navigates to another screen */
    public static Button linkButton(String text) {
        Button b = new Button(text);
        b.setFont(FONT_LINK);
        b.setForeground(PRIMARY_DARK);
        b.setBackground(LIGHT_BG);
        return b;
    }

    public static Choice choice(String... items) {
        Choice c = new Choice();
        c.setFont(FONT_FIELD);
        for (String item : items) {
            c.add(item);
        }
        return c;
    }

    public static Panel titleBar(String title) {
        Panel bar = new Panel(new BorderLayout());
        bar.setBackground(PRIMARY_DARK);
        Label lbl = new Label(title, Label.CENTER);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(WHITE);
        bar.add(lbl, BorderLayout.CENTER);
        bar.setPreferredSize(new Dimension(100, 64));
        return bar;
    }

    public static GridBagConstraints gbc(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.insets = new Insets(10, 12, 10, 12);
        c.anchor = GridBagConstraints.WEST;
        return c;
    }

    public static void showInfo(Window parent, String title, String message, Color color) {
        Dialog dialog = new Dialog(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setBackground(WHITE);

        // A Label can't wrap text or show multiple lines, and a TextArea
        // draws its own native border around the text - neither looks right
        // here. MessageCanvas paints the wrapped, multi-line text itself,
        // so there's no border and long messages still wrap cleanly.
        MessageCanvas msg = new MessageCanvas(message, FONT_FIELD, color, 380);

        Panel msgPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 20, 16));
        msgPanel.setBackground(WHITE);
        msgPanel.add(msg);

        Button ok = primaryButton("OK");
        Panel btnPanel = new Panel();
        btnPanel.setBackground(WHITE);
        btnPanel.add(ok);
        ok.addActionListener(e -> dialog.dispose());

        dialog.add(msgPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        // Size the dialog to fit the wrapped text so it never scrolls.
        Dimension pref = msg.getPreferredSize();
        int width = Math.max(420, pref.width + 80);
        int height = Math.max(190, pref.height + 140);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(parent);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                dialog.dispose();
            }
        });
        dialog.setVisible(true);
    }

    /**
     * Paints word-wrapped, multi-line text with no border and no native
     * text-component chrome - just the plain colored text, centered.
     */
    private static final class MessageCanvas extends Canvas {
        private final java.util.List<String> lines;
        private final Font font;
        private final int lineHeight;
        private final int widestLine;

        MessageCanvas(String text, Font font, Color color, int maxWidth) {
            this.font = font;
            setForeground(color);
            setBackground(WHITE);

            BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = tmp.createGraphics();
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            this.lineHeight = fm.getHeight();
            this.lines = wrap(text, fm, maxWidth);
            int widest = 0;
            for (String line : lines) {
                widest = Math.max(widest, fm.stringWidth(line));
            }
            this.widestLine = Math.min(widest, maxWidth);
            g2.dispose();
        }

        private static java.util.List<String> wrap(String text, FontMetrics fm, int maxWidth) {
            java.util.List<String> result = new java.util.ArrayList<>();
            for (String paragraph : text.split("\n", -1)) {
                if (paragraph.isEmpty()) {
                    result.add("");
                    continue;
                }
                StringBuilder line = new StringBuilder();
                for (String word : paragraph.split(" ")) {
                    String candidate = line.length() == 0 ? word : line + " " + word;
                    if (fm.stringWidth(candidate) > maxWidth && line.length() > 0) {
                        result.add(line.toString());
                        line = new StringBuilder(word);
                    } else {
                        line = new StringBuilder(candidate);
                    }
                }
                result.add(line.toString());
            }
            return result;
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setFont(font);
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            int y = fm.getAscent();
            for (String line : lines) {
                int x = Math.max(0, (getWidth() - fm.stringWidth(line)) / 2);
                g2.drawString(line, x, y);
                y += lineHeight;
            }
        }

        @Override
        public void update(Graphics g) {
            paint(g);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(widestLine, lineHeight * lines.size());
        }
    }

    public static void showSuccess(Window parent, String message) {
        showInfo(parent, "Success", message, SUCCESS);
    }

    public static void showError(Window parent, String message) {
        showInfo(parent, "Error", message, DANGER);
    }
}
