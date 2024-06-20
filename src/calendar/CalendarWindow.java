package calendar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Represents a graphical calendar window.
 */
public class CalendarWindow extends JFrame implements ActionListener {

    private JPanel calendarPanel;
    private YearMonth currentMonth;
    private JLabel monthYearLabel;
    private JButton prevButton, nextButton;
    private Map<LocalDate, ArrayList<String>> events;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    /**
     * Constructor of CalendarView class.
     */
    public CalendarWindow() {
        this.events = new HashMap<>();
        this.currentMonth = YearMonth.now();
        try {
            socket = new Socket("localhost", 2020);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connected to server");
        } catch (IOException e) {
            System.out.println("Client failed to connect to server");
            System.exit(1);
        }
        init();
        loadEventsFromServer();
    }

    /**
     * Loads events from the server.
     */
    private void loadEventsFromServer() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("END")) {
                    break;
                }
                String[] parts = message.split(";");
                LocalDate date = LocalDate.parse(parts[0]);
                String event = parts[1];
                events.computeIfAbsent(date, k -> new ArrayList<>()).add(event);
            }
        } catch (IOException e) {
            System.out.println("Failed to load events from server");
        }
    }

    /**
     * Initializes the calendar window.
     */
    private void init() {
        setTitle("Calendar Planner");
        setSize(500, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        calendarPanel = new JPanel(new GridLayout(0, 7));
        add(calendarPanel, BorderLayout.CENTER);

        monthYearLabel = new JLabel("", SwingConstants.CENTER);

        prevButton = new JButton("<");
        prevButton.addActionListener(this);

        nextButton = new JButton(">");
        nextButton.addActionListener(this);

        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.add(prevButton, BorderLayout.WEST);
        controlPanel.add(nextButton, BorderLayout.EAST);
        controlPanel.add(monthYearLabel, BorderLayout.CENTER);

        add(controlPanel, BorderLayout.NORTH);

        refreshCalendar();
        setVisible(true);
    }

    /**
     * Refreshes the calendar display.
     */
    private void refreshCalendar() {
        calendarPanel.removeAll();
        monthYearLabel.setText(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + currentMonth.getYear());
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            JLabel dayLabel = new JLabel(dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.ENGLISH), SwingConstants.CENTER);
            calendarPanel.add(dayLabel);
        }

        LocalDate firstOfMonth = currentMonth.atDay(1);
        int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7 - 1;
        for (int i = 0; i < startDayOfWeek; i++) {
            calendarPanel.add(new JLabel(""));
        }

        int daysInMonth = currentMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.addActionListener(new DayButton(date, events, out));
            calendarPanel.add(dayButton);
        }
        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == prevButton) {
            currentMonth = currentMonth.minusMonths(1);
            refreshCalendar();
        }
        if (e.getSource() == nextButton) {
            currentMonth = currentMonth.plusMonths(1);
            refreshCalendar();
        }
    }

    public static void main(String[] args) {
        new CalendarWindow();
    }

}
