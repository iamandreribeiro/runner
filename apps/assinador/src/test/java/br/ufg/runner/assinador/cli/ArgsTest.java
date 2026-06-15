package br.ufg.runner.assinador.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArgsTest {

    @Test
    void parseValueOptionSeparatedBySpace() {
        Args a = Args.parser().value("--type")
                .parse(new String[]{"--type", "1.2.3"});
        assertEquals("1.2.3", a.required("--type"));
    }

    @Test
    void parseValueOptionWithEqualsSyntax() {
        Args a = Args.parser().value("--port")
                .parse(new String[]{"--port=8080"});
        assertEquals("8080", a.required("--port"));
    }

    @Test
    void parseBooleanFlagPresent() {
        Args a = Args.parser().flag("--server")
                .parse(new String[]{"--server"});
        assertTrue(a.flag("--server"));
    }

    @Test
    void parseBooleanFlagAbsent() {
        Args a = Args.parser().flag("--server").parse(new String[]{});
        assertFalse(a.flag("--server"));
    }

    @Test
    void rejectUnknownOption() {
        ArgsException ex = assertThrows(ArgsException.class,
                () -> Args.parser().value("--type")
                        .parse(new String[]{"--unknown", "x"}));
        assertTrue(ex.getMessage().contains("desconhecida"));
    }

    @Test
    void rejectValueOptionMissingValue() {
        ArgsException ex = assertThrows(ArgsException.class,
                () -> Args.parser().value("--type").value("--when")
                        .parse(new String[]{"--type", "--when", "x"}));
        assertTrue(ex.getMessage().contains("requer um valor"));
    }

    @Test
    void rejectValueOptionMissingValueAtEnd() {
        ArgsException ex = assertThrows(ArgsException.class,
                () -> Args.parser().value("--type")
                        .parse(new String[]{"--type"}));
        assertTrue(ex.getMessage().contains("requer um valor"));
    }

    @Test
    void rejectRepeatedValueOption() {
        ArgsException ex = assertThrows(ArgsException.class,
                () -> Args.parser().value("--type")
                        .parse(new String[]{"--type", "a", "--type", "b"}));
        assertTrue(ex.getMessage().contains("repetida"));
    }

    @Test
    void rejectRepeatedFlag() {
        ArgsException ex = assertThrows(ArgsException.class,
                () -> Args.parser().flag("--server")
                        .parse(new String[]{"--server", "--server"}));
        assertTrue(ex.getMessage().contains("repetida"));
    }

    @Test
    void rejectFlagWithValue() {
        ArgsException ex = assertThrows(ArgsException.class,
                () -> Args.parser().flag("--server")
                        .parse(new String[]{"--server=x"}));
        assertTrue(ex.getMessage().contains("não aceita valor"));
    }

    @Test
    void rejectPositionalArgument() {
        ArgsException ex = assertThrows(ArgsException.class,
                () -> Args.parser().value("--type")
                        .parse(new String[]{"posicional"}));
        assertTrue(ex.getMessage().contains("posicional"));
    }

    @Test
    void requiredThrowsWhenAbsent() {
        Args a = Args.parser().value("--type").parse(new String[]{});
        assertThrows(ArgsException.class, () -> a.required("--type"));
    }

    @Test
    void getReturnsNullWhenAbsent() {
        Args a = Args.parser().value("--type").parse(new String[]{});
        assertNull(a.get("--type"));
    }

    @Test
    void mixedValueAndFlag() {
        Args a = Args.parser()
                .value("--port").flag("--server")
                .parse(new String[]{"--server", "--port", "8080"});
        assertTrue(a.flag("--server"));
        assertEquals("8080", a.required("--port"));
    }

    @Test
    void builderRejectsInvalidOptionName() {
        assertThrows(IllegalArgumentException.class,
                () -> Args.parser().value("port"));
        assertThrows(IllegalArgumentException.class,
                () -> Args.parser().value("--"));
        assertThrows(IllegalArgumentException.class,
                () -> Args.parser().flag(null));
    }
}
