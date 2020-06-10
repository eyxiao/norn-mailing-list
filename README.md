### Norn Mailing List System

Final project for 6.031 Elements of Software Construction, taken in fall 2019. Norn is a system that helps users execute sequences of operations (e.g. mailing list definitions, set intersections and unions of different mailing lists) on mailing lists so users can easily and efficiently obtain the correct set of emails they need. 

Some additional features that Norn supports are: 
- Executing operations in parallel for better performance
- Detecting and warning users about mailing list cycles in their inputted expressions
- Loading and saving the state of the current list definitions and expressions. 
- Web interface that displays a broken-down visualization of the most recently evaluated list expression.

The key technical concepts covered in this project are: **concurrency** and **thread-safety**, **recursive data types** , **parsing** and abstract syntax trees, **regular expressions**, and **sockets**/**networking**. The key software programming concepts focused on are: test first programming (see `/test/` for the extensive test suite), and of course, Max Goldman's favorite: SFB/ETU/RFC (safe from bugs, easy to understand, and ready for change). :P

### Project Description

Mailing list systems, like MIT’s Moira, are great in part because they are recursive: lists can contain other lists. So course6-all can include course6-students and course6-faculty, and course6-students can include course6-undergrads, course6-grads, and course6-alumni.

But a mailing list system would be even more useful if it could:

- intersect mailing lists: course6-undergrads intersected with mydorm would let you find the Course 6 undergrads who live at mydorm.
- omit individuals or lists: myfloor omitting benbitdiddle would let you plan a surprise party for Ben.

This project is an expressive mailing list system called Norn (the Norse equivalent to Moira). The system can process a list expression whose meaning is a set of recipients that might be pasted into the To: field of an email message. We use ParserLib to parse list expressions and a recursive data type to represent them.


## How to Use

Run Main.java, and you can enter mailing list expressions to evaluate through the console or by going to localhost:8080/eval/{ desired list expression}. Operations supported can be found in the next section.

## Operations
(see more at http://web.mit.edu/6.031/www/fa19/projects/norn/spec/)

### Recipients
A recipient is an email address with a username and domain name, like `bitdiddle@mit.edu`.

Usernames are nonempty case-insensitive strings of letters, digits, underscores, dashes, periods, and plus signs (e.g. bitdiddle+nospam).

Domain names are nonempty case-insensitive strings of letters, digits, underscores, dashes, and periods. This system does not enforce any other constraints on domain names. For example, we don’t care how many dot-separated parts a domain name has, or whether it ends with a valid top-level domain like `.com` or `.edu`.

### List expressions
The simplest list expressions are:
- the empty string, which means no recipients;
- an email address, as defined above, which represents a single recipient;
- a list name, which represents the set of recipients of a mailing list.
List names are nonempty case-insensitive strings of letters, digits, underscores, dashes, and periods (e.g. course.6).

More complex list expressions are constructed with operators `,`, `!`, `*`, `=`, `;`, `|`, and parentheses. For all the operators, list expression subexpressions (named e and f in the definitions below) may be any valid list expression.

### Set operators
A list expression may also use these set operators:

- e`,`f means set union: recipients in either e or f;
- e`!`f means set difference: recipients in e but not in f;
- e`*`f means set intersection: recipients in both e and f.
Note that ! in this language is a binary operator, not a unary operator like it is in Java.

Of these three operators, * has highest precedence, and , has lowest precedence, so `a,b!c*d` is equivalent to `a,(b!(c*d))`. Operators at the same level of precedence group from left to right, so `a!b!c` means `(a!b)!c`. This is the same order as the usual arithmetic operators, in which 9-3-5 means (9-3)-5 rather than 9-(3-5).

Parentheses may be used to group subexpressions:

- `(`e`)` represents the same recipients as e.

### List definitions
A list expression may define a list name:

- listname`=`e defines listname as the expression e, and returns the set of recipients of e.
The name definition operator `=` has lower precedence than `,`, so that `x=a,b` is equivalent to `x=(a,b)`.

Binary operators should be evaluated from left to right, so that list definitions in the left-hand side expression e must be substituted into the right-hand side expression f. For example, the meaning of `(room=alice@mit.edu)*room` should be `alice@mit.edu`.

List definitions nested inside other list definitions, like `a=(b=c)`, are valid, but their behavior is not explicitly specified. The system should do something reasonable and self-consistent.

There are further details about list definition rules in the next section.

### Sequence
A list expression may be a sequence of list expressions separated by semicolons:

- e `;` f represents the recipients produced by f, after substituting the expressions of all named list definitions found in e

For example, consider `x = a@mit.edu,b@mit.edu ; x * b@mit.edu`. After substituting for x in the second part of the expression, this expression is equivalent to `(a@mit.edu,b@mit.edu) * b@mit.edu`, which represents the single recipient `b@mit.edu`.

Note that a semicolon is a separator between expressions, not a terminator. An expression ending with a semicolon, like `a@mit.edu`;, actually ends with the empty expression, so it evaluates to the empty set of recipients.

Semicolons have lower precedence than all the operators above.

### Parallel
Finally, a list expression may be a parallelization of list expressions separated by pipes:

- e `|` f evaluates e and f so that their list definitions may be used elsewhere, and returns the empty set of recipients

Subexpressions e and f are forbidden to define any list names that also appear (either directly, or indirectly through other list names) in the other subexpression: if they do, your system should produce an error with an informative error message. This means, unlike all other binary operators, e and f can safely be evaluated in either order. In particular, the system must evaluate them in parallel.

For example, consider `(x = a@mit.edu | y = b@mit.edu) , x`. The parallel subexpression defines x and y, and evaluates to the empty set of recipients. After substituting for x in the second part, the expression evaluates to `a@mit.edu`.

On the other hand, `x = a@mit.edu | y = x,b@mit.edu` is invalid.

Pipes have the lowest precedence of all the operators.

### Whitespace
Whitespace characters around operators, email addresses, and list names are irrelevant and ignored, so `a,b,c` means the same as `a , b , c`. Whitespace characters are spaces, tabs (\t), carriage returns (\r), and linefeeds (\n).
