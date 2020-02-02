package norn;

import java.io.IOException;

public class VisualizerTest {
    // Testing strategy:
    // Manual tests partition:
    //      expression evaluated includes:
    //          operations:
    //              sequence, parallel, union, intersection, definition,
    //              difference
    //          base:
    //              recipients, listnames, empty
    //          if it contains listnames:
    //              contains previously defined listnames, not previously defined listnames
    //          contains, does not contain recursive redefinition
    //          can be evaluated successfully, has a mail loop, or has an illegal parallel definition
    //      one web user, multiple web users:
    //          if multiple web users:
    //              when a user defines/redefines a listname, all others should
    //              subsequently see the edit
        
    public VisualizerTest() throws IOException {
        Main.main(new String[0]);
    }
    
    // Instructions for manual tests 1-5:
    // 1. Run this file.
    // 2. Enter localhost:8080/eval/expression in your browser (expression is specified
    //      in each manual test).
    // 3. Follow the rest of the instructions in the specific manual test.
    // After each manual test, restart the file before moving on to the next test.
    
    // *** IMPORTANT NOTES: ***
    // - Use %7C instead of | in the url for parallel list expressions.
    // - We ran into some issues with testing on Safari because of its history/autocompleting
    //      urls. If you've entered a similar url before and Safari suggests this url to you,
    //      it'll send a request to the server to evaluate the expression in the suggested url  
    //      even if you don't hit enter/actually use the url. Chrome doesn't have this issue,
    //      and Safari works if you don't have history tracking enabled or if you use a private
    //      window and clear your history if you've evaluated expressions with history on before.
    
    
    // Manual test 1:
    // Covers: expression evaluated includes:
    //              sequence, parallel, union, definition
    //              recipients, listnames, empty
    //         does not contain recursive redefinition
    //         can be evaluated successfully
    //         one web user
    //
    // Expression to enter: (a=a@mit%7Cb=b@mit,c@mit);b,d@mit,
    //
    // 3. The result should be b@mit, c@mit, d@mit.
    // 4. The structure of the list expression should be shown as a tree
    //    below the result. In the tree, sequence is denoted as "sequence", union is denoted
    //    by "∪", empty is denoted by "∅", parallel is denoted by "execute in parallel", and 
    //    definitions are denoted by "definition of (listname)". Make sure that the tree matches
    //    the list expression evaluated.
    // 5. Used listnames should be a and b, pointing to a@mit and b@mit,c@mit respectively.
    
    
    // Manual test 2:
    // Covers: expression evaluated includes:
    //              intersection, difference, sequence
    //              recipients, listnames
    //         can be evaluated successfully
    //         one web user
    //
    // Expression to enter: a=a@mit,b@mit;b=b@mit,c@mit;d=b!a;d*a
    //
    // 3. The result should be empty set.
    // 4. Check to see that the structure of the list expression matches the tree shown in the visualizer
    //    Intersection is denoted as "∩", and difference is denoted as "-".
    // 5. Used listnames should be a pointing to a@mit, b@mit, b pointing at b@mit,c@mit,
    //    d pointing to c@mit.
    
    
    // Manual test 3:
    // Covers: expression contains previously defined listnames, not previously defined listnames
    //         contains recursive redefinition
    //         can be evaluated successfully
    //         one web user
    //
    // First expression to enter: a=a@mit;a=a,b@mit;a
    //
    // 3. The result should be a@mit,b@mit.
    // 4. The expression structure should be a sequence of a (the expression to evaluate) and another 
    //    sequence of a definition of a as a@mit, then redefined as a,b@mit.
    // 5. The used listnames should be a pointing to a@mit, b@mit.
    //
    // Second expression to enter: b=a,c@mit
    //
    // 6. The result should be a@mit,b@mit,c@mit.
    // 7. The expression structure should show a definition of b as the union of a and c@mit, using
    //    the definition of a from the previous expression entered/evaluated.
    // 8. Used listnames should be a pointing to a@mit, b@mit and b pointing to a@mit, b@mit, c@mit.
    
    
    // Manual test 4:
    // Covers: expression has a mail loop
    //         one web user
    //
    // Expression to enter: a=b;b=c;c=a
    //
    // 3. The screen should display the text, "Mailing list expression has a loop".
    // 4. Try evaluating the expression a, the expression b, and the expression c to see what they 
    //    are currently defined as in the context. They should all evaluate to the empty set, and when
    //    evaluating one of them, the other list names should not appear in used listnames. This is to
    //    check that context is not saving any of the definitions in the expression, since there was an
    //    EvaluationException.
    
    
    // Manual test 5:
    // Covers: expression has an illegal parallel definition
    //         one web user
    //
    // Expression to enter: c=a;(a=a@mit%7Cb=c,b@mit)
    //
    // 3. The screen should display the text, "Parallel subexpressions must not define any list names that 
    //    also appear in the other subexpression.
    // 4. Try evaluating the expression a or the expression b -- they should not have definitions and should
    //    currently evaluate to empty set. Try the expression c too -- it should evaluate to the empty set 
    //    as well.
    //
    
    
    // Manual test 6:
    // Covers: multiple web users
    //
    // For this test, open up localhost:8080/eval/ in two different tabs in your browser.
    // 1. In the first tab, evaluate a=a@mit, and check to see that the results and visualization
    //    in that tab is correct (should evaluate to a@mit).
    // 2. Then, in the second tab, evaluate the expression a, and check to see that it also evaluates to
    //    a@mit. This is a check to see that when one user defines a listname, other users should also see
    //    and use this definition.
    // 3. Next, in the first tab, try evaluating the expression a=b;b=a. Check to see that the mail loop error
    //    text is displayed on the page. Then, evaluate the expression a to check that it still evaluates to
    //    a@mit, and that the expression a=b;b=a did not change the definition of a. Check that the definition
    //    of a is also unchanged for the second user by evaluating the list expressions a and b in the second
    //    tab too -- a should evaluate to a@mit, and b should evaluate to the empty set.
    
}
