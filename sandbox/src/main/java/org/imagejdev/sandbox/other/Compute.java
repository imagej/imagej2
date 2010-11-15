
package org.imagejdev.sandbox.other;

/*
 * Example of the RequestResponse design pattern -
 API Design pattern that solves the problem of growing parameters and return values from API methods.
 Beyond its basic purpose, it can help with delivering multiple results and providing the results
 in an incremental way, one by one. It also helps a lot with ability to cancel a computation in
 the middle.
 *
 * from: http://wiki.apidesign.org/index.php?title=APIDesignPatterns:RequestResponse&useskin=monobook
 */
import java.util.List;
import java.util.Map;

public interface Compute {
    public void computeData(Request request, Response response);

    public final class Request {
        // only getters public, rest hidden only for friend code
        Request() {
        }
    }

    public final class Response {
        // only setters public, rest available only for friend code
        private final Map<String,String> result;
        /** Allow access only to friend code */
        Response(Map<String,String> result) {
            this.result = result;
        }

        public void add(String s) {
            result.put(s, s);
        }

        public void addAll(List<String> all) {
            for (String s : all) {
                add(s);
            }
        }

        /** @since 2.0 */
        public void add(String s, String description) {
            result.put(s, description);
        }
    }
}
