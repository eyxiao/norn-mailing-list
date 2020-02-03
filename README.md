### Norn Mailing List System

This was my final project for 6.031 Elements of Software Construction, taken in fall 2019. 

#### Project Description

Mailing list systems, like MIT’s Moira, are great in part because they are recursive: lists can contain other lists. So course6-all can include course6-students and course6-faculty, and course6-students can include course6-undergrads, course6-grads, and course6-alumni.

But a mailing list system would be even more useful if it could:

- intersect mailing lists: course6-undergrads intersected with mydorm would let you find the Course 6 undergrads who live at mydorm.
- omit individuals or lists: myfloor omitting benbitdiddle would let you plan a surprise party for Ben.

This project is an expressive mailing list system called Norn (the Norse equivalent to Moira). The system can process a list expression whose meaning is a set of recipients that might be pasted into the To: field of an email message. We use ParserLib to parse list expressions and a recursive data type to represent them.


### How to Use

Run Main.java, and you can enter mailing list expressions to evaluate through the console or by going to localhost:8080/eval/{ desired list expression}. Operations supported can be found here: http://web.mit.edu/6.031/www/fa19/projects/norn/spec/.
