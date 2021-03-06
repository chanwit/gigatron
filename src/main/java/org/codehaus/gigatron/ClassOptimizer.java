package org.codehaus.gigatron;

public interface ClassOptimizer {

    /**
     * The main entry of optimisation.
     *
     * @param c
     *            A class name. Actually this method uses only its name to perform
     *            optimization.
     * @return a byte array containing optimized class
     */
    public byte[] optimize(String className);

    public void setTransformers(Object[] transformers);

}