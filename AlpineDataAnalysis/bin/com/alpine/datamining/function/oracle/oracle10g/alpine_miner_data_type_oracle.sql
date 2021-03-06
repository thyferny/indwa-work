create or replace type Floatarray is  varray(100000000) of binary_double;
/
create or replace type IntegerArray is  varray(100000000) of Integer;
/
create or replace type Varchar2Array is Varray(100000000) of Varchar2(4000);
/
create or replace type Numberarray is  varray(100000000) of number;
/

create or replace
type FloatArrayArray is  varray(100000000) of FloatArray;
/

create or replace
type Float3Array is  varray(100000000) of FloatArrayArray;
/

create or replace
type IntegerArrayArray is  varray(100000000) of IntegerArray;
/

create or replace
type Varchar2ArrayArray is  varray(100000000) of Varchar2Array;
/

create or replace function alpine_miner_faa2fa(f_arrayarray FloatArrayArray)
return FloatArray
as
i integer := 0;
j integer := 0;
k integer := 0;
f_array FloatArray := FloatArray();
temp FloatArray;
begin
	for i in 1..f_arrayarray.count() loop
		temp := f_arrayarray(i);
		for j in 1..temp.count() loop
			f_array.extend();
			k := k + 1;
			f_array(k) :=  temp(j);
		end loop;
	end loop;
	return f_array;
end;
/

create or replace function alpine_miner_f3a2fa(f3array Float3Array)
return FloatArray
as
i integer := 0;
j integer := 0;
k integer := 0;
l integer := 0;
f_array FloatArray := FloatArray();
f_arrayarray FloatArrayArray := FloatArrayArray();
temp FloatArray;
begin
	for l in 1..f3array.count() loop
		f_arrayarray := f3array(l);
		for i in 1..f_arrayarray.count() loop
			temp := f_arrayarray(i);
			for j in 1..temp.count() loop
				f_array.extend();
				k := k + 1;
				f_array(k) :=  temp(j);
			end loop;
		end loop;
	end loop;
	return f_array;
end;
/

create or replace function alpine_miner_iaa2ia(i_arrayarray IntegerArrayArray)
return IntegerArray
as
i integer := 0;
j integer := 0;
k integer := 0;
i_array IntegerArray := IntegerArray();
temp IntegerArray;
begin
	for i in 1..i_arrayarray.count() loop
		temp := i_arrayarray(i);
		for j in 1..temp.count() loop
			i_array.extend();
			k := k + 1;
			i_array(k) :=  temp(j);
		end loop;
	end loop;
	return i_array;
end;
/


create or replace function alpine_miner_v2aa2v2a(v_arrayarray Varchar2ArrayArray)
return Varchar2Array
as
i integer := 0;
j integer := 0;
k integer := 0;
v_array Varchar2Array := Varchar2Array();
temp Varchar2Array;
begin
	for i in 1..v_arrayarray.count() loop
		temp := v_arrayarray(i);
		for j in 1..temp.count() loop
			v_array.extend();
			k := k + 1;
			v_array(k) :=  temp(j);
		end loop;
	end loop;
	return v_array;
end;
/

create or replace
function alpine_miner_get_fa_element(farray FloatArray, elementindex integer)
return binary_double
as 
begin
	if (elementindex > 0 and elementindex <= farray.count())
	then
		return farray(elementindex);
	else
		return null;
	end if;
end;
/

create or replace
function alpine_miner_get_ia_element(iarray IntegerArray, elementindex integer)
return binary_double
as
begin
  if (elementindex > 0 and elementindex <= iarray.count())
  then
    return iarray(elementindex);
  else
    return null;
  end if;
end;
/
create or replace
function alpine_miner_get_faa_element(farrayarray FloatArrayArray, elementindex number)
return binary_double
as
  countTotal integer := 0;
  i integer := 0;
  farray FloatArray;
begin
  for i in 1..farrayarray.count() loop
    farray := farrayarray(i);
    if (countTotal + farray.count()>= elementindex) then
      return farray(elementindex - countTotal);
    end if;
    countTotal := countTotal + farray.count();
  end loop;
  return null;
end;
/

create or replace
function get_faa(farrayarray FloatArrayArray, elementindex number)
return binary_double
as
  countTotal integer := 0;
  i integer := 0;
  farray FloatArray;
begin
  for i in 1..farrayarray.count() loop
    farray := farrayarray(i);
    if (countTotal + farray.count()>= elementindex) then
      return farray(elementindex - countTotal);
    end if;
    countTotal := countTotal + farray.count();
  end loop;
  return null;
end;
/

create or replace
function alpine_miner_null_to_0(value number)
return number
as
begin
	if(value is null) then return 0; else return value; end if;
end;
/

create or replace
FUNCTION ALPINE_MINER_ARRAY_COUNT
( arrayarray IN FLOATARRAYARRAY
) RETURN INTEGER AS
  countTotal integer := 0;
  i integer := 0;
  farray FloatArray;
BEGIN
  for i in 1..arrayarray.count() loop
    farray := arrayarray(i);
    countTotal := countTotal + farray.count();
  end loop;
  RETURN countTotal;
END ALPINE_MINER_ARRAY_COUNT;
/



create or replace function alpine_miner_get_v2a_element(v2array varchar2Array, elementindex integer) 
return varchar2 as 
begin if (elementindex > 0 and elementindex <= v2array.count()) 
then return v2array(elementindex);
 else return null; 
 end if; 
 end;
 
 /

 create or replace function alpine_miner_get_v2aa_element(v2arrayarray varchar2ArrayArray,
                                                          elementindex number)
   return varchar2 as
   countTotal integer := 0;
   i          integer := 0;
   v2array    varchar2Array;
 begin
   for i in 1 .. v2arrayarray.count() loop
     v2array := v2arrayarray(i);
     if (countTotal + v2array.count() >= elementindex) then
       return v2array(elementindex - countTotal);
     end if;
     countTotal := countTotal + v2array.count();
   end loop;
   return null;
 end;
/
create or replace function alpine_miner_v2aa2faa(column_namearray          varchar2ArrayArray,
                                                 column_array_length_array integerArrayArray)
  return varchar2 as
  i          integer := 0;
  j          integer := 0;
  l          integer := 0;
  i_index    integer := 0;
  j_index    integer := 0;
  firstflag  integer := 1;
  resultvarchar2 varchar2(32767) := '';
  v_array    varchar2Array;
  countTotal integer := 0;
  PRAGMA AUTONOMOUS_TRANSACTION;
begin
  for i in 1 .. column_namearray.count() loop
    v_array    := column_namearray(i);
    countTotal := countTotal + v_array.count();
  end loop;
  resultvarchar2 := resultvarchar2 || 'floatarrayarray(';
  i_index    := trunc(countTotal / 999);
  j_index    := countTotal - 999 * i_index;
  for i in 1 .. i_index loop
    if firstflag = 1 then
      firstflag := 0;
    else
      resultvarchar2 := resultvarchar2 || ',';
    end if;
    resultvarchar2 := resultvarchar2 || 'floatarray(';
    for j in 0 .. 999 loop
      if j != 1 then
        resultvarchar2 := resultvarchar2 || ',';
      end if;
      if alpine_miner_get_iaa_element(column_array_length_array,
                                      (i - 1) * 999 + j) < 1 then
        resultvarchar2 := resultvarchar2 || '"' ||
                      alpine_miner_get_v2aa_element(column_namearray,
                                                    (i - 1) * 999 + j) ||
                      '"';
      else
        l := 1;
        while l <= alpine_miner_get_iaa_element(column_array_length_array,
                                                (i - 1) * 999 + j) loop
          if l != 1 then
            resultvarchar2 := resultvarchar2 || ',';
          end if;
          resultvarchar2 := resultvarchar2 || '(get_faa("' ||
                        alpine_miner_get_v2aa_element(column_namearray,
                                                      (i - 1) * 999 + j) || '",' || l ||
                        ')';
          l          := l + 1;
        end loop;
      end if;
    end loop;
    resultvarchar2 := resultvarchar2 || ')';
  end loop;
  if j_index > 0 then
    if firstflag = 1 then
      firstflag := 0;
    else
      resultvarchar2 := resultvarchar2 || ',';
    end if;
    resultvarchar2 := resultvarchar2 || 'floatarray(';
    for j in 1 .. j_index loop
      if j != 1 then
        resultvarchar2 := resultvarchar2 || ',';
      end if;
      if alpine_miner_get_iaa_element(column_array_length_array,
                                      i_index * 999 + j) < 1 then
        resultvarchar2 := resultvarchar2 || '"' ||
                      alpine_miner_get_v2aa_element(column_namearray,
                                                    i_index * 999 + j) ||
                      '"';
      else
        l := 1;
        while l <= alpine_miner_get_iaa_element(column_array_length_array,
                                                i_index * 999 + j) loop
          if l != 1 then
            resultvarchar2 := resultvarchar2 || ',';
          end if;
          resultvarchar2 := resultvarchar2 || 'get_faa("' ||
                        alpine_miner_get_v2aa_element(column_namearray,
                                                      i_index * 999 + j) || '",' || l ||
                        ')';
          l          := l + 1;
        end loop;
      end if;
    end loop;
    resultvarchar2 := resultvarchar2 || ')';
  end if;
  resultvarchar2 := resultvarchar2 || ')';
  RETURN resultvarchar2;
end alpine_miner_v2aa2faa;

/


create or replace function alpine_miner_v2aa2faaavg(column_namearray          varchar2ArrayArray,
                                                    column_array_length_array integerArrayArray)
  return varchar2 as
  i          integer := 0;
  j          integer := 0;
  l          integer := 0;
  i_index    integer := 0;
  j_index    integer := 0;
  firstflag  integer := 1;
  resultvarchar2 varchar2(32767) := '';
  v_array    varchar2Array;
  countTotal integer := 0;
  PRAGMA AUTONOMOUS_TRANSACTION;
begin
  for i in 1 .. column_namearray.count() loop
    v_array    := column_namearray(i);
    countTotal := countTotal + v_array.count();
  end loop;
  resultvarchar2 := resultvarchar2 || 'floatarrayarray(';
  i_index    := trunc(countTotal / 999);
  j_index    := countTotal - 999 * i_index;
  for i in 1 .. i_index loop
    if firstflag = 1 then
      firstflag := 0;
    else
      resultvarchar2 := resultvarchar2 || ',';
    end if;
    resultvarchar2 := resultvarchar2 || 'floatarray(';
    for j in 0 .. 999 loop
      if j != 1 then
        resultvarchar2 := resultvarchar2 || ',';
      end if;
      if alpine_miner_get_iaa_element(column_array_length_array,
                                      (i - 1) * 999 + j) < 1 then
        resultvarchar2 := resultvarchar2 || 'trunc(avg("' ||
                      alpine_miner_get_v2aa_element(column_namearray,
                                                    (i - 1) * 999 + j) ||
                      '"),10)';
      else
        l := 1;
        while l <= alpine_miner_get_iaa_element(column_array_length_array,
                                                (i - 1) * 999 + j) loop
          if l != 1 then
            resultvarchar2 := resultvarchar2 || ',';
          end if;
          resultvarchar2 := resultvarchar2 ||
                        'trunc(avg(get_faa("' ||
                        alpine_miner_get_v2aa_element(column_namearray,
                                                      (i - 1) * 999 + j) || '",' || l ||
                        ')),10)';
          l          := l + 1;
        end loop;
      end if;
    end loop;
    resultvarchar2 := resultvarchar2 || ')';
  end loop;
  if j_index > 0 then
    if firstflag = 1 then
      firstflag := 0;
    else
      resultvarchar2 := resultvarchar2 || ',';
    end if;
    resultvarchar2 := resultvarchar2 || 'floatarray(';
    for j in 1 .. j_index loop
      if j != 1 then
        resultvarchar2 := resultvarchar2 || ',';
      end if;
      if alpine_miner_get_iaa_element(column_array_length_array,
                                      i_index * 999 + j) < 1 then
        resultvarchar2 := resultvarchar2 || 'trunc(avg("' ||
                      alpine_miner_get_v2aa_element(column_namearray,
                                                    i_index * 999 + j) ||
                      '"),10)';
      else
        l := 1;
        while l <= alpine_miner_get_iaa_element(column_array_length_array,
                                                i_index * 999 + j) loop
          if l != 1 then
            resultvarchar2 := resultvarchar2 || ',';
          end if;
          resultvarchar2 := resultvarchar2 ||
                        'trunc(avg(get_faa("' ||
                        alpine_miner_get_v2aa_element(column_namearray,
                                                      i_index * 999 + j) || '",' || l ||
                        ')),10)';
          l          := l + 1;
        end loop;
      end if;
    end loop;
    resultvarchar2 := resultvarchar2 || ')';
  end if;
  resultvarchar2 := resultvarchar2 || ')';
  RETURN resultvarchar2;
end alpine_miner_v2aa2faaavg;
/

create or replace function alpine_miner_get_iaa_element(iarrayarray  IntegerArrayArray,
                                                         elementindex integer)
   return float as
   countTotal integer := 0;
   i          integer := 0;
   iarray     IntegerArray;
 begin
   for i in 1 .. iarrayarray.count() loop
     iarray := iarrayarray(i);
     if (countTotal + iarray.count() >= elementindex) then
       return iarray(elementindex - countTotal);
     end if;
     countTotal := countTotal + iarray.count();
   end loop;
   return null;
 end alpine_miner_get_iaa_element;
/


	create or replace type FloatarraySumImpl as object
(
  varraysum Floatarray,
  static function ODCIAggregateInitialize(fs IN OUT FloatarraySumImpl) 
    return number,
  member function ODCIAggregateIterate(self IN OUT FloatarraySumImpl, 
    value IN Floatarray) return number,
  member function ODCIAggregateTerminate(self IN FloatarraySumImpl, 
    returnValue OUT Floatarray, flags IN number) return number,
  member function ODCIAggregateMerge(self IN OUT FloatarraySumImpl, 
    fs2 IN FloatarraySumImpl) return number
)
	;
/
	
	create or replace type body FloatarraySumImpl is 
static function ODCIAggregateInitialize(fs IN OUT FloatarraySumImpl) 
return number is 
arraysum Floatarray := floatArray();
begin
  fs := FloatarraySumImpl(floatArray());
  fs.varraysum := (arraysum);
  return ODCIConst.Success;
end;

member function ODCIAggregateIterate(self IN OUT FloatarraySumImpl, value IN Floatarray) return number is
i integer := 0;
begin
  if self.varraysum.count() = 0 then
    for i in 1..value.count() loop
      self.varraysum.extend();
      self.varraysum(i) := 0.0;
    end loop;
  end if;
  for i in 1..value.count() loop
    self.varraysum(i) := self.varraysum(i) + value(i);
  end loop;
  return ODCIConst.Success;
end;

member function ODCIAggregateTerminate(self IN FloatarraySumImpl, 
    returnValue OUT Floatarray, flags IN number) 
return number is
begin
  returnValue := self.varraysum;
  return ODCIConst.Success;
end;

member function ODCIAggregateMerge(self IN OUT FloatarraySumImpl, fs2 IN FloatarraySumImpl) 
return number is
i integer :=0;
mincount integer := 0;
begin
  if self.varraysum.count() < fs2.varraysum.count() then
    for i in 1..self.varraysum.count() loop
      self.varraysum(i) := self.varraysum(i) + fs2.varraysum(i);
    end loop;
    for i in (self.varraysum.count()+1)..fs2.varraysum.count() loop
      self.varraysum.extend();
      self.varraysum(i) := fs2.varraysum(i);
    end loop;
  else
    for i in 1..fs2.varraysum.count() loop
      self.varraysum(i) := self.varraysum(i) + fs2.varraysum(i);
    end loop;
  end if;
  return ODCIConst.Success;
end;
end;
/

create or replace
FUNCTION FloatarraySum (input FloatArray) RETURN FloatArray 
 PARALLEL_ENABLE  AGGREGATE USING FloatarraySumImpl;
/

CREATE OR REPLACE FUNCTION floatarraysum_cursor(sqlarray VARCHAR2ARRAY)
  RETURN floatarray AS
  TYPE crt IS REF CURSOR;
  sqlstr varchar2(32767) := ' ';
  c1     crt;
  fa     floatArray;
  ret    floatarray := floatarray();
  i      int := 0;
  j      int := 0;
BEGIN
  for i in 1 .. sqlarray.count() loop
    sqlstr := sqlstr||sqlarray(i);
  end loop;
  open c1 for sqlstr;
  i := 1;
  LOOP
    FETCH c1
      INTO fa;
    EXIT WHEN c1%NOTFOUND;
    IF i = 1 THEN
      for j in 1 .. fa.count() loop
        ret.extend();
        ret(j) := fa(j);
      end loop;
    ELSE
      for j in 1 .. fa.count() loop
        ret(j) := ret(j) + fa(j);
      end loop;
    END IF;
    i := i + 1;
  END LOOP;
  return ret;
END;
/


create or replace
FUNCTION getAMVersion
RETURN varchar2 AS
BEGIN
  return 'Alpine Miner Release 2.8';
END;

/

CREATE OR REPLACE FUNCTION alpine_farray_to_clob(dataarray  floatarray,splitchar varchar2)
return clob is
indexnumber binary_double;
resultdata clob;
begin
indexnumber:=1;
resultdata:='';
dbms_lob.append(resultdata,to_char(dataarray(1)));
for indexnumber in 2 .. dataarray.count() loop
	  dbms_lob.append( resultdata,splitchar||dataarray(indexnumber));

  end loop;

return resultdata;
END alpine_farray_to_clob;

/

CREATE OR REPLACE FUNCTION alpine_farray_to_string(dataarray  floatarray,splitchar varchar2)
return varchar2 is
indexnumber binary_double;
resultdata varchar2(32767);
begin
indexnumber:=1;
resultdata:=dataarray(1);
for indexnumber in 2 .. dataarray.count() loop
    resultdata:=resultdata||splitchar||dataarray(indexnumber);

  end loop;

return resultdata;
END alpine_farray_to_string;

/

CREATE OR REPLACE FUNCTION alpine_varray_to_clob(dataarray  varchar2array,splitchar varchar2)
return clob is
indexnumber binary_double;
resultdata clob;
begin
indexnumber:=1;
resultdata:=dataarray(1);
for indexnumber in 2 .. dataarray.count() loop
	  dbms_lob.append( resultdata,splitchar||dataarray(indexnumber));

  end loop;

return resultdata;
END alpine_varray_to_clob;


/


CREATE OR REPLACE FUNCTION alpine_varray_to_string(dataarray  varchar2array,splitchar varchar2)
return varchar2 is
indexnumber binary_double;
resultdata varchar2(32767);
begin
indexnumber:=1;
resultdata:=dataarray(1);
for indexnumber in 2 .. dataarray.count() loop
	  resultdata:=resultdata||splitchar||dataarray(indexnumber);

  end loop;

return resultdata;
END alpine_varray_to_string;

/


CREATE OR REPLACE FUNCTION alpine_iarray_to_clob(dataarray IntegerArray,
                                                 splitchar varchar2)
  return clob is
  indexnumber integer;
  resultdata  clob:=empty_clob();
begin
  indexnumber := 1;
  resultdata  := '';
  resultdata:=resultdata||dataarray(1);
  for indexnumber in 2 .. dataarray.count() loop
     dbms_lob.append(resultdata, splitchar || dataarray(indexnumber));
  end loop;
  return resultdata;
END alpine_iarray_to_clob;


/

CREATE OR REPLACE FUNCTION alpine_iarray_to_string(dataarray  IntegerArray,splitchar varchar2)
return varchar2 is
indexnumber integer;
resultdata varchar2(32767);
begin
indexnumber:=1;
resultdata:=dataarray(1);
for indexnumber in 2 .. dataarray.count() loop
    resultdata:=resultdata||splitchar||dataarray(indexnumber);

  end loop;

return resultdata;
END alpine_iarray_to_string;

/