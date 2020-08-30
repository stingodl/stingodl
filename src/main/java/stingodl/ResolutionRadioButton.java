package stingodl;

import javafx.scene.control.RadioButton;

public class ResolutionRadioButton extends RadioButton {
    public int res;
    public ResolutionRadioButton(int res, String label) {
        super(label);
        this.res = res;
    }
}
