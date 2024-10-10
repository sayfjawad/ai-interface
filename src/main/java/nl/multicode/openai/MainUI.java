package nl.multicode.openai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class MainUI {

    private static ChatLanguageModel selectedModel;
    private static ChatMemory chatMemory;

    public static void main(String[] args) {

        SwingUtilities.invokeLater(MainUI::createAndShowGUI);
    }

    private static void createAndShowGUI() {

        JFrame frame = new JFrame("AI Service Selector");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setLayout(new BorderLayout());

        // Create a dropdown for selecting the AI service
        String[] aiServices = {"OpenAI", "HuggingFace"};
        JComboBox<String> aiServiceDropdown = new JComboBox<>(aiServices);

        // Create a text field for the API key input (password-like)
        JPasswordField apiKeyInput = new JPasswordField(20);
        apiKeyInput.setBorder(BorderFactory.createTitledBorder("Enter API Key (optional)"));

        // Create a text area for the prompt input
        JTextArea promptInput = new JTextArea(5, 40);
        promptInput.setBorder(BorderFactory.createTitledBorder("Enter your prompt"));

        // Create a text area for the conversation output
        JTextArea conversationOutput = new JTextArea(10, 40);
        conversationOutput.setEditable(false);
        conversationOutput.setBorder(BorderFactory.createTitledBorder("Conversation History"));

        // Create a button to send the prompt
        JButton sendButton = new JButton("Send");

        // Add components to the frame
        JPanel topPanel = new JPanel();
        topPanel.add(aiServiceDropdown);
        topPanel.add(apiKeyInput);
        frame.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(promptInput, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(conversationOutput), BorderLayout.CENTER);
        frame.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(sendButton);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        // Initialize chat memory
        chatMemory = TokenWindowChatMemory.withMaxTokens(300, new OpenAiTokenizer());

        // Action listener for the send button
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String selectedService = (String) aiServiceDropdown.getSelectedItem();
                String userPrompt = promptInput.getText();
                String apiKey = new String(apiKeyInput.getPassword());

                if (userPrompt.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter a prompt.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Initialize the selected model based on user selection
                switch (selectedService) {
                    case "OpenAI":
                        selectedModel = OpenAiChatModel.withApiKey(!apiKey.isEmpty() ? apiKey : ApiKeys.OPENAI_API_KEY);
                        break;
                    case "HuggingFace":
                        selectedModel = HuggingFaceChatModel.withAccessToken(!apiKey.isEmpty() ? apiKey : ApiKeys.HF_API_KEY);
                        break;
                    default:
                        JOptionPane.showMessageDialog(frame, "Invalid AI service selected.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                }

                // Add user message to chat memory
                chatMemory.add(UserMessage.userMessage(userPrompt));

                // Generate AI response
                AiMessage aiResponse = selectedModel.generate(chatMemory.messages()).content();

                // Add response to chat memory and update conversation output
                chatMemory.add(aiResponse);
                conversationOutput.append("You: " + userPrompt + "\n");
                conversationOutput.append("AI: " + aiResponse.text() + "\n\n");

                // Clear the prompt input
                promptInput.setText("");
            }
        });

        // Display the frame
        frame.setVisible(true);
    }
}