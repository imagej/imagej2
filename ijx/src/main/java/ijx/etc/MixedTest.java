package ijx.etc;

import ijx.etc.NonMixedFactory.Callback;

public class MixedTest {

    public MixedTest() {
    }

    // BEGIN: sidemeanings.Mixed.Clean.Use
     public void useWithoutMixedMeanings() {
        class AddFiveMixedCounter implements NonMixedFactory.Provider {
            private Callback callback;
            
            public int toBeImplementedBySubclass() {
                callback.toBeCalledBySubclass();
                return 5;
            }

            public void initialize(Callback c) {
                callback = c;
            }
        }
        NonMixed add5 = NonMixedFactory.create(new AddFiveMixedCounter());
//        assertEquals("5/1 = 5", 5, add5.apiForClients());
//        assertEquals("10/2 = 5", 5, add5.apiForClients());
//        assertEquals("15/3 = 5", 5, add5.apiForClients());
    }
    // END: sidemeanings.Mixed.Clean.Use
}

