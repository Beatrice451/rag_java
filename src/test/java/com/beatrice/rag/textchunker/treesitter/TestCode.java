package com.beatrice.rag.textchunker.treesitter;

public class TestCode {
    // This method SHOULD NOT be edited, deleted or rearranged with the others!
    // It is used for tests (As well as the others but this method is the reference)
    public void testSayHelloWorld() {
        System.out.println("Hello, world");
    }

    public void testSayHelloWithName(String name) {
        System.out.printf("Hello, %s!", name);
    }

    public int testAdd(int a, int b) {
        return a + b;
    }

    public int testFactorial(int n) {
        if (n == 0) {
            return 1;
        }
        return n * testFactorial(n - 1);
    }
}
