x=20; y=30; size=18;
interval=1; //seconds
i = floor(i*interval); // 'i' is the image index
setFont("SansSerif", size, "antialiased");
setColor("white");
s = ""+pad(floor(i/3600))+":"+pad(floor((i/60)%60))+":"+pad(i%60);
drawString(s, x, y);
function pad(n) {
  str = toString(n);
  if (lengthOf(str)==1) str="0"+str;
  return str;
}

