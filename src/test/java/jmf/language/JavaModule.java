package jmf.language;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import jmf.data.*;
import jmf.module.AbstractModule;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

/**
 * @brief Testmodule-class of the interoperability test that is used at Java-side
 * @details JavaModules are started from the Java-side within the interoperability test between Java and C++.
 * They are kept simple and only contain functionality that is necessary for the test, such as subscribe or publish,
 * send a request, set an additional state or check if their registry contains a peer with certain properties.
 * @author Matthias Blohm
 * @date created on 10/9/15.
 */
public class JavaModule extends AbstractModule {
    /// counter for incoming pub-messages
    public int receivedReq = 0;
    /// counter for incoming pub-messages
    public int receivedPub = 0;

    /** Constructor sets class attributes such as moduleId, name, version and dependencies
     * @param typeId The typeId the module should have
     * @param instanceId The instanceId the module should have
     * @param version The version the module has
     * @param name The name of this module
     * @param dependencies The vector that contains the dependencies of this module
     */
    public JavaModule(int typeId, int instanceId,int version, String name,Collection<ModuleDependency> dependencies) {
        super(new ModuleUniqueId(UnsignedInteger.fromIntBits(typeId), UnsignedLong.fromLongBits(instanceId)), UnsignedInteger.fromIntBits(version), name, dependencies);
    }

    /** Dummy-method that enables the module and subscribes already to the pub-messages that will come from C++ at the same time
     * @return returns success of enabling module (always true)
     */
    @Override
    public boolean enable() {
        getFramework().subscribe(new MessageType("c++".getBytes()), (msg, sender) -> {
            if (new String(msg.getType().getMatch()).equals("c++") && new String(msg.getData()).equals("c++-publish")) {
                receivedPub++;
            }
        });

        return true;
    }

    /** Dummy-method that is called when the module is disabled (empty) */
    @Override
    public void disable() {

    }

    /** Publishes a dummy-message from java that should arrive at the subscribers at C++-side*/
    public void doPub() {
        getFramework().publish(new Message(new MessageType("java".getBytes()), "java-publish".getBytes()));
    }

    /** Sends out a request-message to a C++-module for testing purposes
     * @param targetId The uniqueId of the module in the network that the message should be sent to
     */
    public void doReq(ModuleUniqueId targetId) {
        try {
            getFramework().sendRequest(targetId, new Message(new MessageType("test".getBytes()), "java-request".getBytes())).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /** Handles an incoming request-message from the network
     * @param message The message that has been received by this module
     * @param sender The uniqueId of the module that has sent the message
     */
    @Override
    public OutReply handleRequest(final Message message, final ModuleUniqueId sender) {
        if (new String(message.getData()).equals("c++-request")){
            receivedReq++;
        }

        return OutReply.createImmediateReply(message);
    }

    /** Sets the additional state of the module
     * @param newAddState The new additional state the module should be set to
     */
    public void setAdditionalState(byte[] newAddState){
        getFramework().onModuleAdditionalStateChanged(newAddState);
    }



    /** Checks if the module knows an active peer with the given uniqueId
     * @param cPartnerId The uniqueId of the C++-module to look for
     * @return returns whether the check was successful or not
     */
    public boolean checkPartnerFound(ModuleUniqueId cPartnerId){
        final ModuleHandle handle = getFramework().getPeerRegistry().getPeerWithId(cPartnerId,true);
        if (handle != null){
            return true;
        }
        return false;
    }


    /** Checks if the module knows a peer with the given lifecycle state
     * @param cPartnerId The uniqueId of the module to look for
     * @param partnerState The lifecycle state the searched module should have
     * @return returns whether the check was successful or not
     */
    public boolean checkPartnerState(ModuleUniqueId cPartnerId,ModuleLifecycleState partnerState){
        final ModuleHandle handle = getFramework().getPeerRegistry().getPeerWithId(cPartnerId, false);
        if (handle != null){
            ModuleLifecycleState actualPartnerState = getFramework().getPeerRegistry().getPeerState(handle);
            if (actualPartnerState == partnerState){
                return true;
            } else {
                return false;
            }
        } else if (handle == null && partnerState == ModuleLifecycleState.Dead ){
            return true;
        }
        return false;
    }


    /** Checks if the module knows a peer with the given information
     * @param cPartnerId The uniqueId of the module to look for
     * @param name The name the searched module should have
     * @param version The version the searched module should have
     * @param additionalState The additional state the searched module should have
     * @return returns whether the check was successful or not
     */
    public boolean checkPartnerInfos(ModuleUniqueId cPartnerId,String name, short version, byte[] additionalState){
        final ModuleHandle handle = getFramework().getPeerRegistry().getPeerWithId(cPartnerId,true);
        if (handle == null){
            return false;
        }
        short p_version = handle.getVersion();
        String p_name = handle.getName();
        byte[] p_addState= getFramework().getPeerRegistry().getPeerAdditionalState(handle);

        if (!(name.equals(p_name))){
            return false;
        }
        if (!(version == p_version)){
            return false;
        }
        if (p_addState.length != additionalState.length){
            return false;
        }
        if (!(Arrays.equals(p_addState, additionalState))){
            return false;
        }

        return true;
    }
}