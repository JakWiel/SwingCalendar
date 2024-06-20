package calendar;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.*;
import java.io.*;

/**
 * Represents a DayButton that handles actions when a day button in the calendar
 * is clicked.
 */
public class DayButton implements ActionListener {

    private LocalDate date;
    private Map<LocalDate, ArrayList<String>> events;
    private PrintWriter out;

    /**
     * Constructor of DayButton class.
     *
     * @param date The date associated with the button.
     * @param events Map to store events for each date.
     * @param out PrintWriter to send data to the server.
     */
    public DayButton(LocalDate date, Map<LocalDate, ArrayList<String>> events, PrintWriter out) {
        this.date = date;
        this.events = events;
        this.out = out;
    }

    /**
     * Returns window associated with the specific date button.
     *
     * @return Window of specific date.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        ArrayList<String> eventsForDate = events.getOrDefault(date, new ArrayList<>());
        
        // Frame of the new window of the date
        JFrame frame = new JFrame("Events for " + date.toString());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        JPanel panel = new JPanel(new BorderLayout());

        JList<String> eventList = new JList<>(eventsForDate.toArray(new String[0]));
        JScrollPane scrollPane = new JScrollPane(eventList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));

        // Button to add new event
        JButton addButton = new JButton("Add Event");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String event = JOptionPane.showInputDialog(frame, "Enter new event:");
                if (event != null && !event.isEmpty()) {
                    eventsForDate.add(event);
                    events.put(date, eventsForDate);
                    eventList.setListData(eventsForDate.toArray(new String[0]));
                    out.println(date.toString() + ";" + event);
                }
            }
        });

        // Button to delete selected event
        JButton deleteButton = new JButton("Delete Event");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = eventList.getSelectedIndex();
                if (selectedIndex != -1) {
                    eventsForDate.remove(selectedIndex);
                    events.put(date, eventsForDate);
                    eventList.setListData(eventsForDate.toArray(new String[0]));
                }
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
    }
}
