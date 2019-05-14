package com.helloworld.flow

import co.paralleluniverse.fibers.Suspendable
import com.helloworld.contract.IOUContract
import com.helloworld.contract.IOUContract.Companion.IOU_CONTRACT_ID
import com.helloworld.state.IOUState
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class IOUFlow(val iouValue: Int,
              val otherParty: Party) : FlowLogic<SignedTransaction>() {

    /** The progress tracker provides checkpoints indicating the progress of the flow to observers. */
    override val progressTracker = ProgressTracker()

    /** The flow logic is encapsulated within the call() method. */
    @Suspendable
    override fun call(): SignedTransaction {
        // We retrieve the notary identity from the network map.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        // We create the transaction components.
        val outputState = IOUState(iouValue, ourIdentity, otherParty)
        val cmd = Command(IOUContract.Commands.Create(), ourIdentity.owningKey)

        // We create a transaction builder and add the components.
        val txBuilder = TransactionBuilder(notary = notary)
                .addOutputState(outputState, IOU_CONTRACT_ID)
                .addCommand(cmd)

        // We sign the transaction.
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        // We finalise the transaction.
        return subFlow(FinalityFlow(signedTx))
    }
}
