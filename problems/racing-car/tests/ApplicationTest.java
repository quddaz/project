class ApplicationTest extends NsTest {

    @Test
    void run_success_byValidInput() {
        run("pobi,woni", "1");
        assertThat(output()).isNotEmpty();
    }

    @Test
    void runException_fail_byInvalidInput() {
        assertThatThrownBy(() -> runException("pobi,javaji", "1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Override
    public void runMain() {
        Application.main(new String[]{});
    }
}
