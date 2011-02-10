package ijx.etc;

// BEGIN: sidemeanings.Mixed.Clean.Factory
public final class NonMixedFactory {
    private NonMixedFactory() {
    }
    
    public static NonMixed create(Provider impl) {
        NonMixed api = new NonMixed(impl);
        Callback callback = new Callback(api);
        impl.initialize(callback);
        return api;
    }

    public interface Provider {
        public void initialize(Callback c);
        public int toBeImplementedBySubclass();
    }

    public static final class Callback {
        final NonMixed api;
        
        Callback(NonMixed api) {
            this.api = api;
        }
        public final void toBeCalledBySubclass() {
            api.counter++;
        }
    }
}
// END: sidemeanings.Mixed.Clean.Factory


