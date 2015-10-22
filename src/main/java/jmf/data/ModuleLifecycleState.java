package jmf.data;

/**
 * Lifecycle state of a module, dead (shut down), inactive (standby) or active
 * Created on 7/25/15.
 * @author Jonas Grunert
 */
public enum ModuleLifecycleState {
    Dead, // = 0
    Inactive, // = 1
    Active // = 2
}