package com.wyc.cloudapp.print;

import androidx.annotation.NonNull;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.print
 * @ClassName: PrintItem
 * @Description: 单个打印项目
 * @Author: wyc
 * @CreateDate: 2021-12-28 15:01
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-12-28 15:01
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class PrintItem {
    public enum Align{
        LEFT,CENTRE,RIGHT
    }
    public enum LineSpacing {
        SPACING_2,SPACING_10,SPACING_DEFAULT
    }

    private String content = "";
    private int fontSize = 9;
    private Align align = Align.LEFT;
    private boolean doubleHigh = false;
    private boolean doubleWidth = false;
    private boolean italic = false;
    private boolean bold = false;
    private boolean newline = true;
    private LineSpacing lineSpacing = LineSpacing.SPACING_DEFAULT;

    public boolean isNewline() {
        return newline;
    }

    public void setNewline(boolean newline) {
        this.newline = newline;
    }

    public LineSpacing getLineSpacing() {
        return lineSpacing;
    }

    public void setLineSpacing(LineSpacing lineSpacing) {
        this.lineSpacing = lineSpacing;
    }


    public String getContent() {
        return content == null ? "" : content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public Align getAlign() {
        return align;
    }

    public void setAlign(Align align) {
        this.align = align;
    }

    public boolean isDoubleHigh() {
        return doubleHigh;
    }

    public void setDoubleHigh(boolean doubleHigh) {
        this.doubleHigh = doubleHigh;
    }

    public boolean isDoubleWidth() {
        return doubleWidth;
    }

    public void setDoubleWidth(boolean doubleWidth) {
        this.doubleWidth = doubleWidth;
    }

    public boolean isItalic() {
        return italic;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public static class Builder{
        final PrintItem item = new PrintItem();
        public Builder setContent(String content) {
            item.setContent(content);
            return this;
        }
        public Builder setFontSize(int fontSize) {
            item.setFontSize(fontSize);
            return this;
        }
        public Builder setAlign(Align align) {
            item.setAlign(align);
            return this;
        }
        public Builder setDoubleHigh(boolean doubleHigh) {
            item.setDoubleHigh(doubleHigh);
            return this;
        }
        public Builder setDoubleWidth(boolean doubleWidth) {
            item.setDoubleWidth(doubleWidth);
            return this;
        }
        public Builder setItalic(boolean italic) {
            item.setItalic(italic);
            return this;
        }
        public Builder setBold(boolean bold) {
            item.setBold(bold);
            return this;
        }
        public Builder setLineSpacing(LineSpacing newline){
            item.setLineSpacing(newline);
            return this;
        }
        public PrintItem build(){
            return item;
        }
    }

    @NonNull

    @Override
    public String toString() {
        return "PrintItem{" +
                "content='" + content + '\'' +
                ", fontSize=" + fontSize +
                ", align=" + align +
                ", doubleHigh=" + doubleHigh +
                ", doubleWidth=" + doubleWidth +
                ", italic=" + italic +
                ", bold=" + bold +
                ", newline=" + newline +
                ", lineSpacing=" + lineSpacing +
                '}';
    }
}
