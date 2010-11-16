package ijx.etc;

import ijx.etc.NonMixedFactory.Provider;

// BEGIN: sidemeanings.Mixed.Clean
public final class NonMixed {
    int counter;
    private int sum;
    private final Provider impl;
    
    NonMixed(Provider impl) {
        this.impl = impl;
    }

    public final int apiForClients() {
        int subclass = impl.toBeImplementedBySubclass();
        sum += subclass;
        return sum / counter;
    }
}
// END: sidemeanings.Mixed.Clean


