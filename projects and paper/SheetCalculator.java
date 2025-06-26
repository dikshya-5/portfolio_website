import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SheetCalculator extends JFrame {
    private JTextField scaleField, zoneField, eastingField, northingField;
    private JTextArea resultArea;

    public SheetCalculator() {
        setTitle("Map Scale Calculator");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(4, 2));
        inputPanel.add(new JLabel("Scale (e.g., 100000):"));
        scaleField = new JTextField();
        inputPanel.add(scaleField);

        inputPanel.add(new JLabel("Central Meridian (81, 84, 87):"));
        zoneField = new JTextField();
        inputPanel.add(zoneField);

        inputPanel.add(new JLabel("Easting:"));
        eastingField = new JTextField();
        inputPanel.add(eastingField);

        inputPanel.add(new JLabel("Northing:"));
        northingField = new JTextField();
        inputPanel.add(northingField);

        // Calculate button
        JButton calculateButton = new JButton("Calculate");
        calculateButton.addActionListener(this::calculateSheetNumber);

        // Result area
        resultArea = new JTextArea();
        resultArea.setEditable(false);

        // Add components to the frame
        add(inputPanel, BorderLayout.NORTH);
        add(resultArea, BorderLayout.CENTER);
        add(calculateButton, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void calculateSheetNumber(ActionEvent e) {
        try {
            int scale = Integer.parseInt(scaleField.getText());
            int zone = Integer.parseInt(zoneField.getText());
            int easting = Integer.parseInt(eastingField.getText());
            int northing = Integer.parseInt(northingField.getText());

            // Validate inputs
            if (zone != 81 && zone != 84 && zone != 87) {
                resultArea.setText("Invalid central meridian. Use 81, 84, or 87.");
                return;
            }
            if (easting < 350000 || easting > 650000 || northing < 2900000 || northing > 3400000) {
                resultArea.setText("Invalid coordinates. Easting: 350000-650000, Northing: 2900000-3400000.");
                return;
            }

            // Calculate sheet number
            int departure = easting - 350000;
            int latitude = 3400000 - northing;

            int[] extent = {50000, 1250, 625, 250};
            int[] sheets = {6, 40, 2, 5};
            int[] sheet = new int[3]; // Sheet number parts, maximum 3 ota tukra ma aauna payo

            for (int i = 0; i < sheets.length && i < extent.length; i++) {
                if (scale == 100000 && i > 0) break; // Only use first extent for 1:100000
                if (scale == 2500 && i > 1) break;   // Use first two extents for 1:2500
                if (scale == 1250 && i > 2) break;   // Use first three extents for 1:1250
                if (scale == 500 && i > 3) break;    // Use all extents for 1:500

                int col = departure / extent[i];
                int row = latitude / extent[i];
                departure %= extent[i];
                latitude %= extent[i];
                sheet[i] = 1 + row * sheets[i] + col;
            }

            // Adjust for zone
            if (zone == 84) sheet[0] += 60;
            if (zone == 87) sheet[0] += 120;

            // Format sheet number
            String sheetNumber = String.format("%03d-%04d", sheet[0], sheet[1]);
            if (scale == 1250) sheetNumber += "-" + sheet[2];
            if (scale == 500) sheetNumber += "-" + String.format("%02d", sheet[2]);

            resultArea.setText("Sheet Number: " + sheetNumber);
        } catch (NumberFormatException ex) {
            resultArea.setText("Invalid input. Please enter numbers only.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SheetCalculator());
    }
}