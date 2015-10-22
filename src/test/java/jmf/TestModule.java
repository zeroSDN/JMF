package jmf;

import com.google.common.primitives.UnsignedInteger;
import jmf.data.*;
import jmf.module.AbstractModule;

import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * TODO Descrive
 * Created on 8/23/15
 * @author Jonas Grunert
 */
public class TestModule extends AbstractModule {

    TestModule(ModuleUniqueId uniqueId, UnsignedInteger version, String name, Collection<ModuleDependency> dependencies)  {
        super(uniqueId, version, name, dependencies);
    }

    @Override
    public boolean enable() {
        System.out.println("enable");
        doSub();
        return true;
    }

    @Override
    public void disable() {
        System.out.println("disable");
    }



    private void onSub(Message msg, ModuleUniqueId sender) {
        System.out.println("sub received: " + msg.getType().getMatch() + " | " + msg.getData() + " | from " +
                sender.toString());

    }

    public void doSub() {
        getFramework().subscribe(new MessageType("ayy".getBytes()), new BiConsumer<Message, ModuleUniqueId>() {
            @Override
            public void accept(Message message, ModuleUniqueId moduleUniqueId) {
                TestModule.this.onSub(message, moduleUniqueId);
            }
        });
    }

    public void doPub() {
        getFramework().publish(new Message("ayyy".getBytes(), "ebin".getBytes()));
    }

    public void doReq() {
        ModuleHandle handle = getFramework().getPeerRegistry().getAnyPeerWithType((short)(13), true);
        if (handle != null) {
            Message request = new Message("simbly".getBytes(), "ebin".getBytes());
            Message reply = null;
            try {
                reply = getFramework().sendRequest(handle.getUniqueId(), request).get();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            System.out.println("received reply! " + reply.getType().getMatch() + " | " + reply.getData());
        }
    }

    @Override
    public OutReply handleRequest(Message message, ModuleUniqueId sender) {
        return OutReply.createImmediateReply(message);
    }
}
