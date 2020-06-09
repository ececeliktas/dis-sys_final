import com.google.gson.Gson;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.WordUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class MainWindow extends JFrame {

    private Gson gson = new Gson();

    public static void main(String... args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow main = new MainWindow();
            main.setVisible(true);
        });
    }

    public MainWindow() {
        // initial setup
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width / 3, screenSize.height / 3);
        setResizable(false);
        setTitle("Distributed Systems Final Project");
        setLocationRelativeTo(null);

        // add a tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        SaveCityPanel saveCityPanel = new SaveCityPanel();
        setupSaveCity(saveCityPanel);
        tabbedPane.addTab("Save City", saveCityPanel);

        ListCitiesPanel listCitiesPanel = new ListCitiesPanel();
        setupListCities(listCitiesPanel);
        tabbedPane.addTab("List Cities", listCitiesPanel);

        add(tabbedPane);
    }

    private void setupListCities(ListCitiesPanel listCitiesPanel) {
        listCitiesPanel.refreshButton.addActionListener(e -> {
            listCitiesPanel.citiesArea.setText(null);

            listCitiesPanel.refreshButton.setEnabled(false);

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet getMethod = new HttpGet("https://citylocations.herokuapp.com/list");
                try (CloseableHttpResponse response = httpClient.execute(getMethod)) {
                    String body = StringEscapeUtils.unescapeJava(EntityUtils.toString(response.getEntity()));
                    listCitiesPanel.citiesArea.setText(body);
                    listCitiesPanel.refreshButton.setEnabled(true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void setupSaveCity(SaveCityPanel saveCityPanel) {
        saveCityPanel.saveCityButton.addActionListener(e -> {
            float x, y;

            try {
                x = Float.parseFloat(saveCityPanel.cityXCoordTextField.getText());
            } catch (NumberFormatException ex) {
                saveCityPanel.messageLabel.setText(saveCityPanel.cityXCoordTextField.getText() + " is not a valid x coordinate");
                return;
            }

            try {
                y = Float.parseFloat(saveCityPanel.cityYCoordTextField.getText());
            } catch (NumberFormatException ex) {
                saveCityPanel.messageLabel.setText(saveCityPanel.cityYCoordTextField.getText() + " is not a valid y coordinate");
                return;
            }

            City city = new City(0, saveCityPanel.cityNameTextField.getText(), x, y);
            String obj = gson.toJson(city);

            saveCityPanel.saveCityButton.setEnabled(false);

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                StringEntity requestEntity = new StringEntity(obj, ContentType.APPLICATION_JSON);
                HttpPost postMethod = new HttpPost("https://citylocations.herokuapp.com/save");
                postMethod.setEntity(requestEntity);

                try (CloseableHttpResponse rawResponse = httpClient.execute(postMethod)) {
                    String body = StringEscapeUtils.unescapeJava(EntityUtils.toString(rawResponse.getEntity()));
                    saveCityPanel.messageLabel.setText(WordUtils.wrap(body, 50));
                }
            } catch (Exception ex) {
                saveCityPanel.messageLabel.setText(ex.getMessage());
            }

            saveCityPanel.saveCityButton.setEnabled(true);
        });

        saveCityPanel.clearCitiesButton.addActionListener(e -> {
            saveCityPanel.clearCitiesButton.setEnabled(false);
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet getMethod = new HttpGet("https://citylocations.herokuapp.com/clear");
                try (CloseableHttpResponse response = httpClient.execute(getMethod)) {
                    String body = StringEscapeUtils.unescapeJava(EntityUtils.toString(response.getEntity()));
                    saveCityPanel.messageLabel.setText(WordUtils.wrap(body, 50));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            saveCityPanel.clearCitiesButton.setEnabled(true);
        });
    }

    private static class ListCitiesPanel extends JPanel {

        JButton refreshButton;
        JTextArea citiesArea;

        ListCitiesPanel() {
            setLayout(null);

            this.citiesArea = new JTextArea();
            this.citiesArea.setSize(725, 410);
            this.citiesArea.setEditable(false);

            JScrollPane scroll = new JScrollPane (this.citiesArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scroll.setSize(this.citiesArea.getSize());
            add(scroll);

            this.refreshButton = new JButton("Refresh");
            refreshButton.setSize(100, 30);
            refreshButton.setLocation(this.citiesArea.getX() + this.citiesArea.getWidth(), 0);
            add(refreshButton);
        }

    }

    private static class SaveCityPanel extends JPanel {

        JTextField cityNameTextField;
        JTextField cityXCoordTextField;
        JTextField cityYCoordTextField;
        JButton saveCityButton;
        JButton clearCitiesButton;
        JTextArea messageLabel;

        SaveCityPanel() {
            setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;

            JLabel cityNameLabel = new JLabel("City Name:");
            c.gridx = 0;
            c.gridy = 0;
            add(cityNameLabel, c);

            this.cityNameTextField = new JTextField(15);
            c.gridx = 1;
            c.gridy = 0;
            add(cityNameTextField, c);

            JLabel cityXCoordLabel = new JLabel("City X:");
            c.gridx = 0;
            c.gridy = 1;
            add(cityXCoordLabel, c);

            this.cityXCoordTextField = new JTextField(15);
            c.gridx = 1;
            c.gridy = 1;
            add(cityXCoordTextField, c);

            JLabel cityYCoordLabel = new JLabel("City Y:");
            c.gridx = 0;
            c.gridy = 2;
            add(cityYCoordLabel, c);

            this.cityYCoordTextField = new JTextField(15);
            c.gridx = 1;
            c.gridy = 2;
            add(cityYCoordTextField, c);

            this.saveCityButton = new JButton("Save");
            c.gridx = 0;
            c.gridy = 3;
            c.gridwidth = 2;
            add(saveCityButton, c);

            this.clearCitiesButton = new JButton("Clear All Cities");
            Icon icon = UIManager.getIcon("OptionPane.warningIcon");
            BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bufferedImage.createGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            ImageIcon warningIcon = new ImageIcon(bufferedImage.getScaledInstance(15, 15, Image.SCALE_SMOOTH));
            this.clearCitiesButton.setIcon(warningIcon);
            c.gridx = 0;
            c.gridy = 4;
            c.gridwidth = 2;
            add(clearCitiesButton, c);

            this.messageLabel = new JTextArea();
            this.messageLabel.setEditable(false);
            this.messageLabel.setBackground(getBackground());
            c.gridx = 0;
            c.gridy = 5;
            c.gridwidth = 2;
            add(messageLabel, c);
        }

    }

}
