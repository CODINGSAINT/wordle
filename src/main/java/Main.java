import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    static List<Character> notPresent = new ArrayList<>();
    static List<Character> present = new ArrayList<>();
    static Map<Integer, Character> atPos = new HashMap<>();
    static int trials = 0;

    public static void main(String[] args) {
        wordle();
    }


    static void wordle() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );
            boolean solved = false;
            while (!solved) {
                String word = null;
                Page page = browser.newPage();
                page.navigate("https://www.powerlanguage.co.uk/wordle/");
                page.click("game-icon[icon='close']");
                List<String> words = load();
                trials = 1;
                while (trials <= 6) {

                    //evaluate
                    Integer evaluate = null;
                    while (evaluate == null) {
                 /*   if (trials == 0)
                        word = "aldol";
                    else*/
                        word = words.get(new Random().nextInt(0, words.size()));
                        System.out.println("Evaluating word \t" + word);
                        typeWord(page, word);
                        evaluate = evaluate(page, word, trials);
                        if (evaluate == null) {
                            clear(page);
                        } else if (evaluate == 5) {
                            System.out.println("result = " + word);
                            System.out.println();
                            solved = true;
                            break;
                        } else {
                            trials++;
                            List<String> copyWords = new ArrayList<>(words);
                            copyWords.remove(word);
                            words = updateWords(copyWords);
                            System.out.println("Words List Updated " + words.size() + " Try Row No " + (trials) + "\n" + words);

                        }
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            System.exit(1);
        }
    }

    /**
     * There can be words which are not in the wordle db, it will clear them
     *
     * @param page
     * @throws InterruptedException
     */
    static void clear(Page page) throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            page.click("button[data-key=←]");
        }
        page.waitForTimeout(1000);
    }

    static List<String> updateWords(List<String> currentWords) {
        List<String> updaedWords = currentWords.stream().filter(filter()).toList();
        return updaedWords;
    }

    static void typeWord(Page page, String word) throws InterruptedException {
        for (char c : word.toCharArray()) {
            page.click("button[data-key=" + c + "]");
        }
        page.click("button[data-key=↵]");
        page.waitForTimeout(1500);
    }

    static Integer evaluate(Page page, String word, int row) {

        var elements = page.querySelectorAll("#board game-row:nth-child(" + (row) + ") game-tile");
        var results = elements
                .stream().map(e -> e.getAttribute("evaluation")).toList();
        if (results.size() == 0)
            return null;

        Integer exactMatch = 0;

        for (int i = 0; i < results.size(); i++) {
            String currentResult = results.get(i);
            if (currentResult == null) return null;
            switch (currentResult) {

                case "present" -> present.add(word.charAt(i));
                case "absent" -> notPresent.add(word.charAt(i));
                case "correct" -> {
                    exactMatch++;
                    atPos.put(i, word.charAt(i));
                }

            }
        }

        return exactMatch;
    }

    static Predicate<String> filter() {
        //Remove all present fom notPresent and contails - Because of double letter words
        notPresent.removeAll(present);

        notPresent.removeAll(atPos.values());

        Predicate<String> filter = new Predicate<String>() {
            @Override
            public boolean test(String s) {
                boolean np = notPresent.stream().allMatch(character -> s.indexOf(character) < 0);
                if (!np) return false;
                boolean p = present.stream().allMatch(character -> s.indexOf(character) >= 0);
                if (!p) return false;
                Boolean positionVerified = true;
                Set<Integer> set = atPos.keySet();
                for (Integer pos : set) {
                    if (s.charAt(pos) != atPos.get(pos)) {
                        return false;
                    }
                }

                return true;
            }
        };
        return filter;
    }

    static List<String> load() {
        Main m = new Main();
        ClassLoader classLoader = m.getClass().getClassLoader();
        URL resource = classLoader.getResource("words.txt");
        try (Stream<String> lines = Files.readAllLines(new File(resource.getFile()).toPath()).stream()) {
            return lines.collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return List.of();
    }
}
