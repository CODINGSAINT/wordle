# wordle solver
This is a simple attempt to solve wordle using *Java* and it's filter with Predicate
Steps are simple
- Solve untill not found the right answer
    - Load all of the words from file (created via https://www-cs-faculty.stanford.edu/~knuth/sgb-words.txt)
    - Pick random word
    - if (solved) exit
        - else filter the words as
            - Not found (absent)
            - found (present)
            - at correct place (correct)
        - repeat

# Required
It required
- Java 17
- Playwright 1.17.0
