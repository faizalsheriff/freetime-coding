package com.datastructures.linear.arrays;

public class ArraysRandom {

	




/**
 * @param args
 */
public static void main(String[] args) {
/* String s=  "Sourcetemptemplategene";
String t= "template";
char a [][]={{'a','b','c','d'},{'e','f','g','h'},{'a','b','c','d'},{'e','f','g','h'}};
achTemplateGenerator(a);
System.out.println(isSquare(a));
indexOf(s.toCharArray(), 0, s.length(), t.toCharArray(), 0, t.length(),0);
if(s.contains(t))
System.out.println(true);
else
System.out.println(false);*/
System.out.println(achThresholdAmountGenerator("abcd".toCharArray(),"dabc".toCharArray()));

char a [][]={{'a','b','c','d'},{'e','f','g','h'},{'i','j','k','l'},{'m','n','o','p'}};
MatrixRotator rd= new MatrixRotator(a);
rd.rotate90C();
rd.display();

}
static int indexOf(char[] source, int sourceOffset, int sourceCount,
             char[] target, int targetOffset, int targetCount,
             int fromIndex) {
if (fromIndex >= sourceCount) {
  return (targetCount == 0 ? sourceCount : -1);
}
if (fromIndex < 0) {
  fromIndex = 0;
}
if (targetCount == 0) {
return fromIndex;
}

char first  = target[targetOffset];
int max = sourceOffset + (sourceCount - targetCount);

for (int i = sourceOffset + fromIndex; i <= max; i++) {
  /* Look for first character. */
  if (source[i] != first) {
      while (++i <= max && source[i] != first);
  }

  /* Found first character, now look at the rest of v2 */
  if (i <= max) {
      int j = i + 1;
      int end = j + targetCount - 1;
      for (int k = targetOffset + 1; j < end && source[j] == 
               target[k]; j++, k++);

      if (j == end) {
          /* Found whole string. */
          return i - sourceOffset;
      }
  }
}
return -1;
}

//ach rule template generator
static void achTemplateGenerator(char[][] template){
if(!isOFAC(template)){
return;
}
int sr=0;  // start pos
int sc=3; // stop pos
int r=sr,c=sc; //loc row col pointer
//for( (r<=3 && c<=3);)
}
//ACH rule template generator
static boolean achThresholdAmountGenerator(char[] tA, char[] tar){
if(tA==null || tar==null || tA.length==0||tar.length==0)
return false;
if(tA.length<tar.length)
return false;
for(int s=0, t=0;s<tA.length;s++){
if(tar[t]==tA[s]){
if(findSub(s,tA, tar))
return true;
}
}
return false;
}


private static boolean findSub(int s, char[] src, char[] tar) {
int ptr=s; //local ptr
int cnt=src.length; //total chars to check
//cnt = cnt-(cnt-tar.length)-1; //refine exact no of chars to check
int t=0; //target string pointer
int i=0; //source string pointer
while(cnt>0){
while(ptr<src.length){ //progress forward
if(src[ptr]==tar[t]){
ptr++;
t++;
i++;
cnt--;
if(i==tar.length){
return true;
}
}else{
return false;
}
}
ptr=0; //rotate
while(ptr<s){
if(src[ptr]==tar[t]){ 
ptr++;
t++;
cnt--;
i++;
if(i==tar.length){
return true;
}
}else{
return false;
}
}
}
return false;
}



















static char[][] ofac90(char[][] in){
if(isOFAC(in)){
int sc=0,sr=0,r=0,c=0,or=0,oc=0, mr=3,mc=3,m=3;//4x4


while(oc<mc&&or<mr){
                        //to copy inside the array 
r=sr;
c=sc;


while(sc<=mc){ 
                        
                        //complete rotate
char cur=in[sr][sc++];
int totC=4*m;
int rowD= 1; //row inc or dec direction 1 -inc 0 -dec
int colD=1;
//single rotate
while(totC>1){
 
totC--;
if(rowD==1&&r<mr){
in[r][c]=in[r++][c];
continue;
}
rowD=0;
//r--;
if(colD==1&&c<mc){
in[r][c]=in[r][++c];
continue;
}
colD=0;
//c--;
if(rowD==0&&r>0){
in[r][c]=in[--r][c];
continue;
}
rowD= 1;
//r++;
if(colD==0&&c>0){
in[r][c]=in[r][--c];
continue;
}
}
    in[r][c]=cur;
}
//decrement row and column to traverse inside template
oc++;
or++;
mr--;
mc--;
sc=oc;
sr=or;
m=m-2; // dec
}
}
return in;
}

static boolean isOFAC(char[][] sq){
int prev=-1;
if(sq.length>0){
if(prev==-1)
prev = sq[0].length;
int i = 0;
for(; (i<sq.length && prev == sq[i].length);i++);
if(i==sq.length){
return true;
}
}
return false;
}

}


