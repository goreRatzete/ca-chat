package server;

/**
 * Created by Av on 08-09-2015.
 */
public class ChatProtocol {

    private ConnectionToClient client;
    private String clientName;

    // Client commands
    private static final String USER = "USER";
    private static final String MESSAGE = "MSG";
    private static final String STOP = "STOP";

    // Server commands
    private static final String USERLIST = "USERLIST";


    public ChatProtocol(ConnectionToClient client) {
        this.client = client;

    }

    //
    public Message authentication(Message message, String[] in) {
        // if the first message sent conforms to the protocol, set the username
        if (in[0].equals(USER)) {
            message = Message.SENDTOALL; // update everyone's user list
            client.setClientName(in[1]);
            clientName = in[1];
            message.setContent("USERLIST" + "#" + getUserList());
        } else { // else send an error message
            errorMessage();
        }

        return message;
    }

    public Message processInput(String input) {
        String[] in = input.split("#");
        Message message = new Message();

        // First time
        if (clientName == null) {
            message = authentication(message, in);
        } else {
            // Determine command
            switch (in[0]) {
                case MESSAGE:
                    // determine whether it should send to all or specific users
                    if (in[1].equalsIgnoreCase("ALL")) {
                        message = Message.SENDTOALL;
                    } else {
                        String[] receivers = in[1].split(",");
                        addReceivers(message, receivers);
                    }

                    message.setFrom(client);

                    message.setContent("MSG" + "#" + client.getClientName() + "#" + in[2]);
                    break;
                case STOP:

                    break;
                default:
                    errorMessage();
                    break;
            }

        }


        return message;
    }



    // For each receiver String loop through the client list and add the corresponding ConnectionToClient to the list of receivers
    private void addReceivers(Message message, String[] receivers) {
        for (String receiver : receivers) {
            for (ConnectionToClient client : Server.getClients()) {
                if (receiver.equals(client.getClientName())) {
                    message.getTo().add(client);
                }
            }
        }
    }

    private String getUserList() {
        StringBuilder stringBuilder = new StringBuilder();
        for (ConnectionToClient client : Server.getClients()) {
            stringBuilder.append(client.getClientName() + ",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();

    }

    private Message errorMessage() {
        System.out.println("Message does not conform to the protocol!");
        Message message = Message.ERROR;
        message.getTo().add(client);
        return message;
    }



}