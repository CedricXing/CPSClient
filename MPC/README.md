### Model file format

##### <param name = "x" type="real"...

##### \<invariant>... &amp; ... &amp; ... \</invariant>

##### \<transition source="$" target="$">
##### \<label>...\</label>
##### The format of location's name is v$ in which $ means no. (mandatory)

##### assignments support the following format
```v'=...[a,b]...```

### CFG file format
##### initially = "x1==a1 & x2==a2 & x3==a3"
##### forbidden = "x1>a5 & x2>=a6"
##### forbidden does not support "=" now

##### It is mandatory that"loc()==v$" appears in initially.
##### Init value only support [a,b] now, not support [] +...


