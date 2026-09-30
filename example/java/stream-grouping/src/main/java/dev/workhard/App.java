package dev.workhard;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class App {
    record Book(String title, String category, int price) {}

    static Map<String, Integer> totalPriceByCategory(List<Book> books) {
        return books.stream().collect(Collectors.groupingBy(
                Book::category, TreeMap::new, Collectors.summingInt(Book::price)));
    }

    public static void main(String[] args) {
        List<Book> books = List.of(
                new Book("Java", "dev", 30), new Book("Node", "dev", 25), new Book("Cook", "life", 12));
        System.out.println(totalPriceByCategory(books));
    }
}
