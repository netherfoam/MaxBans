package org.maxgamer.maxbans.banmanager;

import java.util.HashMap;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class TrieSet
{
    private TrieNode top;
    
    public TrieSet() {
        super();
        this.top = new TrieNode();
    }
    
    private TrieNode getNode(final String s) {
        TrieNode node = this.top;
        for (int i = 0; i < s.length(); ++i) {
            final Character c = s.charAt(i);
            node = node.get(c);
            if (node == null) {
                return null;
            }
        }
        return node;
    }
    
    private Path nearestPath(final String s) {
        final List<Path> potential = new ArrayList<Path>();
        final List<Path> next = new ArrayList<Path>();
        final TrieNode node = this.getNode(s);
        if (node == null) {
            return null;
        }
        final Path top = new Path(node);
        potential.add(top);
        while (!potential.isEmpty()) {
            for (final Path p : potential) {
                if (p.getNode().isWord()) {
                    return p;
                }
                for (final Map.Entry<Character, TrieNode> entry : p.getNode().getChildMap().entrySet()) {
                    final Character c = entry.getKey();
                    final TrieNode child = entry.getValue();
                    final Path q = new Path(p, child, c);
                    next.add(q);
                }
            }
            potential.clear();
            potential.addAll(next);
            next.clear();
        }
        return null;
    }
    
    public String nearestKey(final String s) {
        Path p = this.nearestPath(s);
        if (p == null) {
            return null;
        }
        final char[] key = new char[p.getDepth()];
        while (p.getChar() != null) {
            key[p.getDepth() - 1] = p.getChar();
            p = p.getPrevious();
        }
        return String.valueOf(s) + new String(key);
    }
    
    public boolean contains(final String s) {
        final TrieNode node = this.getNode(s);
        return node != null && node.isWord();
    }
    
    public boolean remove(final String s) {
        final int length = s.length();
        if (length == 0) {
            return this.top.setWord(false);
        }
        TrieNode fork;
        TrieNode node = fork = this.top;
        Character forkChar = s.charAt(0);
        for (int i = 0; i < length; ++i) {
            final Character c = s.charAt(i);
            if (node.isWord() || node.getChildMap().size() > 1) {
                fork = node;
                forkChar = c;
            }
            node = node.get(c);
            if (node == null) {
                return false;
            }
        }
        if (node == null) {
            return false;
        }
        if (node.getChildMap().isEmpty()) {
            fork.remove(forkChar);
            return node.isWord();
        }
        return node.setWord(false);
    }
    
    public boolean add(final String key) {
        final int length = key.length();
        if (length != 0) {
            TrieNode node = this.top;
            int i = 0;
            do {
                final Character c = key.charAt(i);
                final TrieNode parent = node;
                node = node.get(c);
                if (node == null) {
                    node = parent.put(c);
                }
            } while (++i < length);
            return node.setWord(true);
        }
        if (this.top.isWord()) {
            return true;
        }
        this.top.setWord(true);
        return false;
    }
    
    public HashSet<String> matches(final String s) {
        final HashSet<String> keys = new HashSet<String>();
        TrieNode node = this.top;
        for (int i = 0; i < s.length(); ++i) {
            final Character c = s.charAt(i);
            node = node.get(c);
            if (node == null) {
                return keys;
            }
        }
        if (node.isWord()) {
            keys.add(s);
        }
        final HashSet<String> nodes = node.getChildKeys();
        for (final String key : nodes) {
            keys.add(String.valueOf(s) + key);
        }
        return keys;
    }
    
    public void clear() {
        this.top = new TrieNode();
    }
    
    public void debug() {
        final PrintStream out = System.out;
        out.println("Size: " + this.size() + ", Empty: " + this.isEmpty());
        LinkedList<TrieNode> nodes = new LinkedList<TrieNode>();
        nodes.add(this.top);
        while (!nodes.isEmpty()) {
            final LinkedList<TrieNode> next = new LinkedList<TrieNode>();
            for (final TrieNode node : nodes) {
                System.out.print("   " + node.getChildMap().size() + "   ");
                next.addAll(node.getChildMap().values());
            }
            System.out.println();
            nodes = next;
        }
    }
    
    public boolean isAmbiguous(final String s) {
        final TrieNode base = this.getNode(s);
        if (base == null) {
            return false;
        }
        LinkedList<TrieNode> nodes = new LinkedList<TrieNode>();
        nodes.add(base);
        while (!nodes.isEmpty()) {
            final LinkedList<TrieNode> next = new LinkedList<TrieNode>();
            if (nodes.size() > 1) {
                return true;
            }
            for (final TrieNode node : nodes) {
                next.addAll(node.getChildMap().values());
            }
            nodes = next;
        }
        return false;
    }
    
    public int size() {
        int size = 0;
        LinkedList<TrieNode> nodes = new LinkedList<TrieNode>();
        nodes.add(this.top);
        while (!nodes.isEmpty()) {
            final LinkedList<TrieNode> subNodes = new LinkedList<TrieNode>();
            for (final TrieNode node : nodes) {
                if (node.isWord()) {
                    ++size;
                }
                subNodes.addAll(node.getChildMap().values());
            }
            nodes = subNodes;
        }
        return size;
    }
    
    public HashSet<String> values() {
        return this.matches("");
    }
    
    public boolean isEmpty() {
        return !this.top.isWord() && this.top.getChildMap().isEmpty();
    }
    
    private class Path
    {
        private Path previous;
        private TrieNode node;
        private Character c;
        private int depth;
        
        public Path(final Path previous, final TrieNode node, final Character c) {
            super();
            if (node == null) {
                throw new NullPointerException("Null node given");
            }
            this.c = c;
            this.previous = previous;
            this.node = node;
            this.depth = previous.getDepth() + 1;
        }
        
        public Path(final TrieNode node) {
            super();
            if (node == null) {
                throw new NullPointerException("Null path given");
            }
            this.node = node;
            this.depth = 0;
        }
        
        public int getDepth() {
            return this.depth;
        }
        
        public Path getPrevious() {
            return this.previous;
        }
        
        public TrieNode getNode() {
            return this.node;
        }
        
        public Character getChar() {
            return this.c;
        }
    }
    
    private class TrieNode
    {
        private boolean isWord;
        private HashMap<Character, TrieNode> children;
        
        public TrieNode(){
        	this(false);
        }
        /*
        public TrieNode(final TrieSet set) {
            this(set, false);
        }
        */
        public TrieNode(final boolean isWord) {
            super();
            this.children = new HashMap<Character, TrieNode>(5);
            this.isWord = isWord;
        }
        
        public boolean isWord() {
            return this.isWord;
        }
        
        public TrieNode get(final Character c) {
            return this.children.get(c);
        }
        
        public TrieNode remove(final Character c) {
            return this.children.remove(c);
        }
        
        public TrieNode put(final Character c) {
            final TrieNode node = new TrieNode(true);
            this.children.put(c, node);
            return node;
        }
        
        public HashSet<String> getChildKeys() {
            final HashSet<String> values = new HashSet<String>();
            for (final Map.Entry<Character, TrieNode> entry : this.children.entrySet()) {
                String word = "";
                final TrieNode node = entry.getValue();
                final Character key = entry.getKey();
                if (node.isWord()) {
                    values.add(key.toString());
                }
                word = String.valueOf(word) + key;
                for (final String child : node.getChildKeys()) {
                    final String childKey = String.valueOf(word) + child;
                    values.add(childKey);
                }
            }
            return values;
        }
        
        public HashMap<Character, TrieNode> getChildMap() {
            return this.children;
        }
        
        public boolean setWord(final boolean isWord) {
            final boolean old = this.isWord;
            this.isWord = isWord;
            return old;
        }
    }
}
