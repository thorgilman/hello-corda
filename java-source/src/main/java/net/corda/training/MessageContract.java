package net.corda.training;

import net.corda.core.contracts.*;
import net.corda.core.transactions.LedgerTransaction;
import java.util.Collections;
import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

/* Contract */
public class MessageContract implements Contract {

    static final String ID = "net.corda.training.MessageContract";
    static class SendMessage extends TypeOnlyCommandData implements CommandData {}

    @Override
    public void verify(LedgerTransaction tx) {

        final CommandWithParties<SendMessage> command = requireSingleCommand(tx.getCommands(), SendMessage.class);

        requireThat( require -> {
            require.using("Must be a SendMessage command", command.getValue().equals(new SendMessage()));
            require.using("There should one output state.", tx.getOutputStates().size() == 1);
            final MessageState state = tx.outputsOfType(MessageState.class).get(0);
            require.using("The party sending the message must sign the SendMessage transaction.", (command.getSigners().equals(Collections.singletonList(state.origin.getOwningKey()))));

            // require.using("There should be no input state.", tx.getInputStates().isEmpty());
            return null;
        });
    }
}
