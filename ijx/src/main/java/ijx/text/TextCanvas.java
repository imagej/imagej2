package ijx.text;
import ijx.util.Java2;
import java.awt.*;
import java.awt.event.*;

class TextCanvas extends Canvas {

	TextPanel tp;
	Font fFont;
	FontMetrics fMetrics;
	Graphics gImage;
	Image iImage;
	boolean antialiased;

	TextCanvas(TextPanel tp) {
		this.tp = tp;
		addMouseListener(tp);
		addMouseMotionListener(tp);
		addKeyListener(tp);
		addMouseWheelListener(tp);
	}

    public void setBounds(int x, int y, int width, int height) {
    	super.setBounds(x, y, width, height);
		tp.adjustVScroll();
		tp.adjustHScroll();
    	iImage = null;
    }

	public void update(Graphics g) {
		paint(g);
	}
  
	public void paint(Graphics g) {
		if(tp==null || g==null) return;
		Dimension d = getSize();
		int iWidth = d.width;
		int iHeight = d.height;
		
		if(iWidth<=0 || iHeight<=0) return;
		g.setColor(Color.lightGray);
		if(iImage==null)
			makeImage(iWidth,iHeight);
		if(tp.iRowHeight==0 || (tp.iColWidth[0]==0&&tp.iRowCount>0)) {
			tp.iRowHeight=fMetrics.getHeight()+2;
			for(int i=0;i<tp.iColCount;i++)
				calcAutoWidth(i);
			tp.adjustHScroll();
			tp.adjustVScroll();
		}
		gImage.setColor(Color.white);
		gImage.fillRect(0,0,iWidth,iHeight);
		if (tp.headings)
			drawColumnLabels(iWidth);
		int y=tp.iRowHeight+1-tp.iY;
		int j=0;
		while(y<tp.iRowHeight+1) {
			j++;
			y+=tp.iRowHeight;
		}
		tp.iFirstRow=j;
		y=tp.iRowHeight+1;
		for(;y<iHeight && j<tp.iRowCount;j++,y+=tp.iRowHeight) {
			int x=-tp.iX;
			for(int i=0;i<tp.iColCount;i++) {
				int w=tp.iColWidth[i];
				Color b=Color.white,t=Color.black;
				if(j>=tp.selStart && j<=tp.selEnd) {
					int w2 = w;
					if (tp.iColCount==1)
						w2 = iWidth;
					b=Color.black;
					t=Color.white;
					gImage.setColor(b);
					gImage.fillRect(x,y,w2-1,tp.iRowHeight);
				}
				gImage.setColor(t);
				char[] chars = getChars(i,j);
				if (chars!=null)
					gImage.drawChars(chars,0,chars.length,x+2,y+tp.iRowHeight-5);
				x+=w;
			}
		}
		if (iImage!=null)
			g.drawImage(iImage,0,0,null);
	}
  
 	void makeImage(int iWidth, int iHeight) {
		iImage=createImage(iWidth, iHeight);
		if (gImage!=null)
			gImage.dispose();
		gImage=iImage.getGraphics();
		gImage.setFont(fFont);
		Java2.setAntialiasedText(gImage, antialiased);
		if(fMetrics==null)
			fMetrics=gImage.getFontMetrics();
	}

 	void drawColumnLabels(int iWidth) {
		gImage.setColor(Color.darkGray);
		gImage.drawLine(0,tp.iRowHeight,iWidth,tp.iRowHeight);
		int x=-tp.iX;
		for(int i=0;i<tp.iColCount;i++) {
			int w=tp.iColWidth[i];
			gImage.setColor(Color.lightGray);
			gImage.fillRect(x+1,0,w,tp.iRowHeight);
			gImage.setColor(Color.black);
			if (tp.sColHead[i]!=null)
				gImage.drawString(tp.sColHead[i],x+2,tp.iRowHeight-5);
			if (tp.iColCount>1) {
				gImage.setColor(Color.darkGray);
				gImage.drawLine(x+w-1,0,x+w-1,tp.iRowHeight-1);
				gImage.setColor(Color.white);
				gImage.drawLine(x+w,0,x+w,tp.iRowHeight-1);
			}
			x+=w;
		}
		gImage.setColor(Color.lightGray);
		gImage.fillRect(0,0,1,tp.iRowHeight);
		gImage.fillRect(x+1,0,iWidth-x,tp.iRowHeight);
		//gImage.drawLine(0,0,0,iRowHeight-1);
		gImage.setColor(Color.darkGray);
		gImage.drawLine(0,0,iWidth,0);
	}
	
	char[] getChars(int column, int row) {
		if (tp==null) return null;
		if (row>=tp.vData.size())
			return null;
		char[] chars = (char[])(tp.vData.elementAt(row));
		if (chars.length==0)
			return null;
		
		if (tp.iColCount==1) {
	    	//for (int i=0; i<chars.length; i++) {
	    	//	if (chars[i]<' ')
	    	//		chars[i] = ' ';
	    	//}
	    	return chars;
	    }
	    
	    int start = 0;
	    int tabs = 0;
	    int length = chars.length;
	    
	    while (column>tabs) {
	    	if (chars[start]=='\t')
	    		tabs++;
	    	start++;
	    	if (start>=length)
	    		return null;
	    };
	    if (start<0 || start>=chars.length) {
			System.out.println("start="+start+", chars.length="+chars.length);	    	
	    	return null;
	    }
	    if (chars[start]=='\t')
	    	return null;
	    
	    int end = start;
	    while (chars[end]!='\t' && end<(length-1))
	    	end++;
	    if (chars[end]=='\t')
	    	end--;
	    	
	    char[] chars2 = new char[end-start+1];
	    for (int i=0,j=start; i<chars2.length; i++,j++) {
	    	chars2[i] = chars[j];
	    } 
		return chars2;
	}
	
	void calcAutoWidth(int column) {
		if (tp.sColHead==null || column>=tp.iColWidth.length || gImage==null)
			return;
		if(fMetrics==null)
			fMetrics=gImage.getFontMetrics();
		int w=15;
		int maxRows;
		if (tp.iColCount==1)
			maxRows = 100;
		else {
			maxRows = 20;
			if (column==0 && tp.sColHead[0].equals(" "))
				w += 5;
			else {
				char[] chars = tp.sColHead[column].toCharArray();
				w = Math.max(w,fMetrics.charsWidth(chars,0,chars.length));
			}
		}
		int rowCount = Math.min(tp.iRowCount, maxRows);
		for(int row=0; row<rowCount; row++) {
			char[] chars = getChars(column,row);
			if (chars!=null)
				w = Math.max(w,fMetrics.charsWidth(chars,0,chars.length));
		}
		//System.out.println("calcAutoWidth: "+column+"  "+tp.iRowCount);
		char[] chars = tp.iRowCount>0?getChars(column, tp.iRowCount-1):null;
		if (chars!=null)
			w = Math.max(w,fMetrics.charsWidth(chars,0,chars.length));
		tp.iColWidth[column] = w+15;
	}

}
