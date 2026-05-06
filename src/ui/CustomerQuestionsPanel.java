package ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.*;
import models.Employee;

public class CustomerQuestionsPanel extends JPanel {
    private final MainFrame frame;
    private final Employee employee;
    private final DefaultListModel<CustomerChatPanel.CustomerQuestion> listModel = new DefaultListModel<>();
    private final JList<CustomerChatPanel.CustomerQuestion> questionList = new JList<>(listModel);
    private final JTextArea detailsArea = new JTextArea(8, 50);

    public CustomerQuestionsPanel(MainFrame frame, Employee employee) {
        this.frame = frame;
        this.employee = employee;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Customer Questions", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));

        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);

        questionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetails(questionList.getSelectedValue());
            }
        });

        JButton resolveButton = new JButton("Resolve Selected");
        JButton respondButton = new JButton("Respond to Selected");
        JButton refreshButton = new JButton("Refresh");
        JButton backButton = new JButton("Back");

        resolveButton.addActionListener(e -> resolveSelectedQuestion());
        respondButton.addActionListener(e -> respondToSelectedQuestion());
        refreshButton.addActionListener(e -> loadQuestions());
        backButton.addActionListener(e -> frame.showEmployeeDashboard(employee));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(respondButton);
        buttonPanel.add(resolveButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(questionList),
                new JScrollPane(detailsArea)
        );
        splitPane.setResizeWeight(0.6);

        add(title, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loadQuestions();
    }

    private void loadQuestions() {
        listModel.clear();
        List<CustomerChatPanel.CustomerQuestion> questions = CustomerChatPanel.getQuestions();
        for (CustomerChatPanel.CustomerQuestion question : questions) {
            listModel.addElement(question);
        }
        if (!listModel.isEmpty()) {
            questionList.setSelectedIndex(0);
        } else {
            detailsArea.setText("No open customer questions.");
        }
    }

    private void updateDetails(CustomerChatPanel.CustomerQuestion question) {
        if (question == null) {
            detailsArea.setText("No question selected.");
            return;
        }

        detailsArea.setText(
                "Customer: " + question.getCustomer().getFirstName() + " " + question.getCustomer().getLastName() + "\n"
                        + "Customer ID: " + question.getCustomer().getAccount_id() + "\n"
                        + "Username: " + question.getCustomer().getUsername() + "\n"
                        + "Status: " + question.getStatus() + "\n"
                        + "Submitted: " + question.getSubmittedAt() + "\n\n"
                        + "Question:\n" + question.getQuestion()
                        + (question.hasResponse()
                        ? "\n\nResponse (" + question.getRespondedAt() + ") from " + question.getResponderName() + ":\n"
                        + question.getResponse()
                        : "")
        );
    }

    private void resolveSelectedQuestion() {
        CustomerChatPanel.CustomerQuestion selectedQuestion = questionList.getSelectedValue();
        if (selectedQuestion == null) {
            JOptionPane.showMessageDialog(this, "Select a question to resolve.");
            return;
        }

        CustomerChatPanel.resolveQuestion(selectedQuestion);
        loadQuestions();
        JOptionPane.showMessageDialog(this, "Question marked as resolved.");
    }

    private void respondToSelectedQuestion() {
        CustomerChatPanel.CustomerQuestion selectedQuestion = questionList.getSelectedValue();
        if (selectedQuestion == null) {
            JOptionPane.showMessageDialog(this, "Select a question to respond to.");
            return;
        }

        JTextArea responseArea = new JTextArea(8, 40);
        responseArea.setLineWrap(true);
        responseArea.setWrapStyleWord(true);
        if (selectedQuestion.hasResponse()) {
            responseArea.setText(selectedQuestion.getResponse());
        }

        int option = JOptionPane.showConfirmDialog(
                this,
                new JScrollPane(responseArea),
                "Respond to Customer",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String responseText = responseArea.getText().trim();
        if (responseText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Response cannot be empty.");
            return;
        }

        String responderName = employee.getFirstName() + " " + employee.getLastName();
        CustomerChatPanel.respondToQuestion(selectedQuestion, responderName, responseText);
        loadQuestions();
        JOptionPane.showMessageDialog(this, "Response sent. Customer will be notified.");
    }
}
