package com.krg.core.utils;

import javax.smartcardio.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * @author Kenny Ric Geron
 * @description Keyboard Wedge utility for Smart Readers
 * @date February 2020
 */
public class App {


    private final static String FORMAT = "%02X";
    private final static String ASTERISK = "*";

    public static void main(String[] args) {
        final TerminalFactory factory = TerminalFactory.getDefault();
        final CardTerminals terminals = factory.terminals();
        final Stack<String> myStack = new Stack<String>();
        System.out.println("######################################");
        System.out.println("Reader is now on monitoring mode..");
        System.out.println("Press Ctrl + C to hang up..");
        System.out.println("######################################");
        execute(terminals, myStack);

    }

    private static void execute(CardTerminals terminals, Stack<String> myStack) {
        String badgeNo = "";

        try {
            final List<CardTerminal> list = terminals.list();
            final CardTerminal terminal = list.get(0);
            boolean isPresent = terminal.waitForCardPresent(999999);

            if (isPresent) {
                final Card card = terminal.connect(ASTERISK);
                final ATR atr = card.getATR();
                final byte[] byteArr = atr.getBytes();
                final StringBuilder sb = new StringBuilder();

                for (int ctr = 0; ctr < byteArr.length; ctr++) {
                    sb.append(String.format(FORMAT, byteArr[ctr]));
                }

                badgeNo = convertToBadgeNumber(sb.toString());
                if (!myStack.isEmpty() && !myStack.pop().equals(badgeNo)) {
                    myStack.push(badgeNo);
                } else {
                    boolean isAbsent = terminal.waitForCardAbsent(0);
                    if (isAbsent) {
                        execute(terminals, myStack);
                    }
                }
            }

        } catch (CardException e) {
            System.out.println(e.toString());
        }
    }

    private static String hexToBin(String s) {
        return new BigInteger(s, 16).toString(2);
    }

    private static String convertToBadgeNumber(String hex) {
        String badgeNo = "";
        final String original = hex != null ? hexToBin(hex) : "";
        final String altered = original != null ? original.substring(0, original.length() - 1) : "";
        final String binarySubstr = altered != null ? altered.substring(altered.length() - 18) : "";

        if (binarySubstr != null || !binarySubstr.equals("")) {
            int decimal = Integer.parseInt(binarySubstr, 2);
            System.out.println("Scanned item: " + decimal);
            badgeNo = Integer.toString(decimal);
            sendKeyPress(badgeNo);
        }

        return badgeNo;
    }

    private static Map<Character, Integer> constructMapKeys() {
        final Map<Character, Integer> map = new HashMap<>();

        map.put('0', KeyEvent.VK_0);
        map.put('1', KeyEvent.VK_1);
        map.put('2', KeyEvent.VK_2);
        map.put('3', KeyEvent.VK_3);
        map.put('4', KeyEvent.VK_4);
        map.put('5', KeyEvent.VK_5);
        map.put('6', KeyEvent.VK_6);
        map.put('7', KeyEvent.VK_7);
        map.put('8', KeyEvent.VK_8);
        map.put('9', KeyEvent.VK_9);

        return map;
    }

    private static void sendKeyPress(String temp) {

        if (temp != null) {
            try {
                final Robot robot = new Robot();
                final Map<Character, Integer> map = constructMapKeys();
                final char[] c = temp.toCharArray();

                for (char value : c) {
                    Integer key = map.get(value);
                    robot.keyPress(key);
                    robot.keyRelease(key);
                }

            } catch (AWTException e) {
                System.out.println(e.toString());
            }
        }
    }
}