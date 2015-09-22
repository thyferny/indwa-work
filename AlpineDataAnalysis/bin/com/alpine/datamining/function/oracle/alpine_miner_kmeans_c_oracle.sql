
 CREATE OR REPLACE FUNCTION ALPINE_MINER_KMEANS_DISTANCE(distancetype  integer,
                                                         column_name   varchar2array,
                                                         column_number integer,
                                                         k             integer)
   return clob as
   caculate_array clob := ' ';
   temp1          clob := ' ';
   temp2          clob := ' ';
   temp3          clob := ' ';
   temp4          clob := ' ';
   i              integer;
   j              integer;
 begin
   if distancetype = 1 then
     i := 1;
     while i < (k + 1) loop
       j := 1;
       dbms_lob.append(caculate_array, '(');
       while j < column_number loop
         dbms_lob.append(caculate_array,
                         '(x."' || column_name(j) ||
                         '"*cast(1.0 as binary_double)-y."k' || i ||
                         column_name(j) || '")*(x."' || column_name(j) ||
                         '"*cast(1.0 as binary_double)-y."k' || i ||
                         column_name(j) || '")+');
         j := j + 1;
       end loop;
       if i = k then
         dbms_lob.append(caculate_array,
                         '(x."' || column_name(j) ||
                         '"*cast(1.0 as binary_double)-y."k' || i ||
                         column_name(j) || '")*(x."' || column_name(j) ||
                         '"*cast(1.0 as binary_double)-y."k' || i ||
                         column_name(j) || '")) as d' || (i - 1));
       else
         dbms_lob.append(caculate_array,
                         '(x."' || column_name(j) ||
                         '"*cast(1.0 as binary_double)-y."k' || i ||
                         column_name(j) || '")*(x."' || column_name(j) ||
                         '"*cast(1.0 as binary_double)-y."k' || i ||
                         column_name(j) || '")) as d' || (i - 1) || ',');
       end if;
       i := i + 1;
     end loop;
   elsif distancetype = 2 then
     i := 1;
     while i < (k + 1) loop
       j     := 1;
       temp1 := '(';
       temp2 := '(';
       while j < column_number loop
         temp1 := temp1 || '(y."k' || i || column_name(j) || '"*ln(y."k' || i ||
                  column_name(j) || '"*cast(1.0 as binary_double)/x."' ||
                  column_name(j) || '"))+';
         temp2 := temp2 || '(y."k' || i || column_name(j) ||
                  '"*cast(1.0 as binary_double)-x."' || column_name(j) ||
                  '")+';
         j     := j + 1;
       end loop;
       temp1 := temp1 || '(y."k' || i || column_name(j) || '"*ln(y."k' || i ||
                column_name(j) || '"*cast(1.0 as binary_double)/x."' ||
                column_name(j) || '")))';
       temp2 := temp2 || '(y."k' || i || column_name(j) ||
                '"*cast(1.0 as binary_double)-x."' || column_name(j) ||
                '"))';
       temp3 := '(' || temp1 || '-' || temp2 || ')';
       if i = k then
         temp3 := temp3 || 'as d' || (i - 1);
       else
         temp3 := temp3 || 'as d' || (i - 1) || ',';
       end if;
       dbms_lob.append(caculate_array, temp3);
       i := i + 1;
     end loop;
   elsif distancetype = 3 then
     i := 1;
     while i < (k + 1) loop
       j := 1;
       if (i = 1) then
         caculate_array := caculate_array || '(';
       else
         dbms_lob.append(caculate_array, '(');
       end if;
       while j < column_number loop
         dbms_lob.append(caculate_array,
                         '(y."k' || i || column_name(j) ||
                         '"*log(2.0,(y."k' || i || column_name(j) ||
                         '"*cast(1.0 as binary_double)/x."' ||
                         column_name(j) || '")))+');
         j := j + 1;
       end loop;
       if i = k then
         dbms_lob.append(caculate_array,
                         '(y."k' || i || column_name(j) ||
                         '"*log(2.0,(y."k' || i || column_name(j) ||
                         '"*cast(1.0 as binary_double)/x."' ||
                         column_name(j) || '")))) as d' || (i - 1));
       else
         dbms_lob.append(caculate_array,
                         '(y."k' || i || column_name(j) ||
                         '"*log(2.0,(y."k' || i || column_name(j) ||
                         '"*cast(1.0 as binary_double)/x."' ||
                         column_name(j) || '")))) as d' || (i - 1) || ',');
       end if;
       i := i + 1;
     end loop;
   elsif distancetype = 4 then
     i := 1;
     while i < (k + 1) loop
       j := 1;
       dbms_lob.append(caculate_array, '(');
       while j < column_number loop
         dbms_lob.append(caculate_array,
                         '(abs((x."' || column_name(j) ||
                         '"*cast(1.0 as binary_double))-(y."k' || i ||
                         column_name(j) || '"))/abs((x."' || column_name(j) ||
                         '"*cast(1.0 as binary_double))+(y."k' || i ||
                         column_name(j) || '")))+');
         j := j + 1;
       end loop;
       if i = k then
         dbms_lob.append(caculate_array,
                         '(abs((x."' || column_name(j) ||
                         '"*cast(1.0 as binary_double))-(y."k' || i ||
                         column_name(j) || '"))/abs((x."' || column_name(j) ||
                         '"*cast(1.0 as binary_double))+(y."k' || i ||
                         column_name(j) || '")))) as d' || (i - 1));
       else
         dbms_lob.append(caculate_array,
                         '(abs((x."' || column_name(j) ||
                         '"*cast(1.0 as binary_double))-(y."k' || i ||
                         column_name(j) || '"))/abs((x."' || column_name(j) ||
                         '"*cast(1.0 as binary_double))+(y."k' || i ||
                         column_name(j) || '")))) as d' || (i - 1) || ',');
       end if;
       i := i + 1;
     end loop;
   elsif distancetype = 5 then
     i := 1;
     while i < (k + 1) loop
       j := 1;
       dbms_lob.append(caculate_array, '(');
       while j < column_number loop
         dbms_lob.append(caculate_array,
                         'abs((x."' || column_name(j) ||
                         '"*cast(1.0 as binary_double))-(y."k' || i ||
                         column_name(j) || '"))+');
         j := j + 1;
       end loop;
       if i = k then
         dbms_lob.append(caculate_array,
                         'abs((x."' || column_name(j) ||
                         '"*cast(1.0 as binary_double))-(y."k' || i ||
                         column_name(j) || '"))) as d' || (i - 1));
       else
         dbms_lob.append(caculate_array,
                         'abs((x."' || column_name(j) ||
                         '"*cast(1.0 as binary_double))-(y."k' || i ||
                         column_name(j) || '"))) as d' || (i - 1) || ',');
       end if;
       i := i + 1;
     end loop;
   elsif distancetype = 6 then
     i := 1;
     while i < (k + 1) loop
       j     := 1;
       temp1 := '(';
       temp2 := '(';
       temp3 := '(';
       while j < column_number loop
         temp1 := temp1 || '(x."' || column_name(j) ||
                  '"*cast(1.0 as binary_double)*y."k' || i ||
                  column_name(j) || '")+';
         temp2 := temp2 || '(x."' || column_name(j) ||
                  '"*cast(1.0 as binary_double)*x."' || column_name(j) ||
                  '")+';
         temp3 := temp3 || '(y."k' || i || column_name(j) ||
                  '"*cast(1.0 as binary_double)*y."k' || i ||
                  column_name(j) || '")+';
         j     := j + 1;
       end loop;
       temp1 := temp1 || '(x."' || column_name(j) ||
                '"*cast(1.0 as binary_double)*y."k' || i || column_name(j) ||
                '"))';
       temp2 := temp2 || '(x."' || column_name(j) ||
                '"*cast(1.0 as binary_double)*x."' || column_name(j) ||
                '"))';
       temp3 := temp3 || '(y."k' || i || column_name(j) ||
                '"*cast(1.0 as binary_double)*y."k' || i || column_name(j) ||
                '"))';
       if i = k then
         temp4 := 'acos(case when (' || temp1 || '/(sqrt(' || temp2 ||
                  ')*sqrt(' || temp3 || ')))>1 then 1 when (' || temp1 ||
                  '/(sqrt(' || temp2 || ')*sqrt(' || temp3 ||
                  ')))<-1 then -1 else (' || temp1 || '/(sqrt(' || temp2 ||
                  ')*sqrt(' || temp3 || '))) end ) as d' || (i - 1);
       else
         temp4 := 'acos(case when (' || temp1 || '/(sqrt(' || temp2 ||
                  ')*sqrt(' || temp3 || ')))>1 then 1 when (' || temp1 ||
                  '/(sqrt(' || temp2 || ')*sqrt(' || temp3 ||
                  ')))<-1 then -1 else (' || temp1 || '/(sqrt(' || temp2 ||
                  ')*sqrt(' || temp3 || '))) end ) as d' || (i - 1) || ',';
       end if;
       dbms_lob.append(caculate_array, temp4);
       i := i + 1;
     end loop;
   elsif distancetype = 7 then
     i := 1;
     while i < (k + 1) loop
       j     := 1;
       temp1 := '(';
       temp2 := '(';
       temp3 := '(';
       while j < column_number loop
         temp1 := temp1 || '(x."' || column_name(j) ||
                  '"*cast(1.0 as binary_double))+';
         temp2 := temp2 || '(y."k' || i || column_name(j) ||
                  '"*cast(1.0 as binary_double))+';
         temp3 := temp3 || '(x."' || column_name(j) ||
                  '"*cast(1.0 as binary_double)*y."k' || i ||
                  column_name(j) || '")+';
         j     := j + 1;
       end loop;
       temp1 := temp1 || '(x."' || column_name(j) || '"))';
       temp2 := temp2 || '(y."k' || i || column_name(j) || '"))';
       temp3 := temp3 || '(x."' || column_name(j) || '"*y."k' || i ||
                column_name(j) || '"))';
       if i = k then
         temp4 := '(-2*' || temp3 || '/(' || temp1 || '+' || temp2 ||
                  ')) as d' || (i - 1);
       else
         temp4 := '(-2*' || temp3 || '/(' || temp1 || '+' || temp2 ||
                  ')) as d' || (i - 1) || ',';
       end if;
       dbms_lob.append(caculate_array, temp4);
       i := i + 1;
     end loop;
   elsif distancetype = 8 then
     i := 1;
     while i < (k + 1) loop
       j := 1;
       dbms_lob.append(caculate_array, '-(');
       while j < column_number loop
         dbms_lob.append(caculate_array,
                         '(x."' || column_name(j) ||
                         '"*cast(1.0 as binary_double)*y."k' || i ||
                         column_name(j) || '")+');
         j := j + 1;
       end loop;
       if i = k then
         dbms_lob.append(caculate_array,
                         '(x."' || column_name(j) ||
                         '"*cast(1.0 as binary_double)*y."k' || i ||
                         column_name(j) || '")) as d' || (i - 1));
       else
         dbms_lob.append(caculate_array,
                         '(x."' || column_name(j) ||
                         '"*cast(1.0 as binary_double)*y."k' || i ||
                         column_name(j) || '")) as d' || (i - 1) || ',');
       end if;
       dbms_lob.append(caculate_array, temp4);
       i := i + 1;
     end loop;
   elsif distancetype = 9 then
     i := 1;
     while i < (k + 1) loop
       j     := 1;
       temp1 := '(';
       temp2 := '(';
       temp3 := '(';
       while j < column_number loop
         temp1 := temp1 || '(x."' || column_name(j) ||
                  '"*cast(1.0 as binary_double))+';
         temp2 := temp2 || '(y."k' || i || column_name(j) ||
                  '"*cast(1.0 as binary_double))+';
         temp3 := temp3 || '(x."' || column_name(j) ||
                  '"*cast(1.0 as binary_double)*y."k' || i ||
                  column_name(j) || '")+';
         j     := j + 1;
       end loop;
       temp1 := temp1 || '(x."' || column_name(j) || '"))';
       temp2 := temp2 || '(y."k' || i || column_name(j) || '"))';
       temp3 := temp3 || '(x."' || column_name(j) || '"*y."k' || i ||
                column_name(j) || '"))';
       if i = k then
         temp4 := '(-' || temp3 || '/(' || temp1 || '+' || temp2 || '-' ||
                  temp3 || ')) as d' || (i - 1);
       else
         temp4 := '(-' || temp3 || '/(' || temp1 || '+' || temp2 || '-' ||
                  temp3 || ')) as d' || (i - 1) || ',';
       end if;
       dbms_lob.append(caculate_array, temp4);
       i := i + 1;
     end loop;
   end if;
   return(caculate_array);
 end alpine_miner_kmeans_distance;
/
 CREATE OR REPLACE FUNCTION ALPINE_MINER_KMEANS_DISTANCE1(distancetype              integer,
                                                          column_namearray          varchar2ArrayArray,
                                                          column_array_length_array integerArrayArray,
                                                          column_number             integer,
                                                          k                         integer)
   return clob as
   caculate_array clob := ' ';
   temp1          clob := ' ';
   temp2          clob := ' ';
   temp3          clob := ' ';
   temp4          clob := ' ';
   i              integer := 1;
   j              integer;
   l              integer;
   m              integer;
   i_array        integerarray;
   countTotal     integer := 0;
 begin
   for i in 1 .. column_array_length_array.count() loop
     i_array := column_array_length_array(i);
     if i_array.count() != 0 then
       for j in 1 .. i_array.count() loop
         if alpine_miner_get_ia_element(i_array, j) = 0 then
           countTotal := countTotal + 1;
         else
           countTotal := countTotal +
                         alpine_miner_get_ia_element(i_array, j);
         end if;
       end loop;
     end if;
   end loop;
   if distancetype = 1 then
     i := 1;
     while i < (k + 1) loop
       m := 1;
       j := 1;
       dbms_lob.append(caculate_array, '(');
       while j < column_number + 1 loop
         if alpine_miner_get_iaa_element(column_array_length_array, j) < 1 then
           dbms_lob.append(caculate_array,
                           '(x."' ||
                           alpine_miner_get_v2aa_element(column_namearray,
                                                         j) ||
                           '"*cast(1.0 as binary_double)-alpine_miner_get_faa_element(y."k' || i || '",' || m ||
                           '))* (x."' ||
                           alpine_miner_get_v2aa_element(column_namearray,
                                                         j) ||
                           '"*cast(1.0 as binary_double)-alpine_miner_get_faa_element(y."k' || i || '",' || m || '))');
           if countTotal != m then
             dbms_lob.append(caculate_array, '+');
           end if;
           m := m + 1;
         else
           l := 1;
           while l <=
                 alpine_miner_get_iaa_element(column_array_length_array, j) loop
             dbms_lob.append(caculate_array,
                             '(alpine_miner_get_faa_element(x."' ||
                             alpine_miner_get_v2aa_element(column_namearray,
                                                           j) || '",' || l ||
                             ')*cast(1.0 as binary_double)-alpine_miner_get_faa_element(y."k' || i || '",' || m ||
                             '))* (alpine_miner_get_faa_element(x."' ||
                             alpine_miner_get_v2aa_element(column_namearray,
                                                           j) || '",' || l ||
                             ')*cast(1.0 as binary_double)-alpine_miner_get_faa_element(y."k' || i || '",' || m || '))');
             if countTotal != m then
               dbms_lob.append(caculate_array, '+');
             end if;
             l := l + 1;
             m := m + 1;
           end loop;
         end if;
         if countTotal + 1 = m then
           dbms_lob.append(caculate_array, ') as d' || (i - 1));
           if i != k then
             dbms_lob.append(caculate_array, ',');
           end if;
         end if;
         j := j + 1;
       end loop;
       i := i + 1;
     end loop;
   elsif distancetype = 2 then
     i := 1;
     while i < (k + 1) loop
       m     := 1;
       j     := 1;
       temp1 := '(';
       temp2 := '(';
       while j < column_number + 1 loop
         if alpine_miner_get_iaa_element(column_array_length_array, j) < 1 then
           temp1 := temp1 || '(alpine_miner_get_faa_element(y."k' || i || '",' || m ||
                    ')*ln(alpine_miner_get_faa_element(y."k' || i || '",' || m ||
                    ')*cast(1.0 as binary_double)/x."' ||
                    alpine_miner_get_v2aa_element(column_namearray, j) ||
                    '"))';
           temp2 := temp2 || '(alpine_miner_get_faa_element(y."k' || i || '",' || m ||
                    ')*cast(1.0 as binary_double)-x."' ||
                    alpine_miner_get_v2aa_element(column_namearray, j) || '")';
           if countTotal != m then
             temp1 := temp1 || '+';
             temp2 := temp2 || '+';
           end if;
           m := m + 1;
         else
           l := 1;
           while l <=
                 alpine_miner_get_iaa_element(column_array_length_array, j) loop
             temp1 := temp1 || '(alpine_miner_get_faa_element(y."k' || i || '",' || m ||
                      ')*ln(alpine_miner_get_faa_element(y."k' || i || '",' || m ||
                      ')*cast(1.0 as binary_double)/alpine_miner_get_faa_element(x."' ||
                      alpine_miner_get_v2aa_element(column_namearray, j) || '",' || l ||
                      ')))';
             temp2 := temp2 || '(alpine_miner_get_faa_element(y."k' || i || '",' || m ||
                      ')*cast(1.0 as binary_double)-alpine_miner_get_faa_element(x."' ||
                      alpine_miner_get_v2aa_element(column_namearray, j) || '",' || m || '))';
             if countTotal != m then
               temp1 := temp1 || '+';
               temp2 := temp2 || '+';
             end if;
             l := l + 1;
             m := m + 1;
           end loop;
         end if;
         if countTotal + 1 = m then
           temp1 := temp1 || ')';
           temp2 := temp2 || ')';
         end if;
         j := j + 1;
       end loop;
       temp3 := '(' || temp1 || '-' || temp2 || ')';
       if i = k then
         temp3 := temp3 || 'as d' || (i - 1);
       else
         temp3 := temp3 || 'as d' || (i - 1) || ',';
       end if;
       dbms_lob.append(caculate_array, temp3);
       i := i + 1;
     end loop;
   elsif distancetype = 3 then
     i := 1;
     while i < (k + 1) loop
       m := 1;
       j := 1;
       dbms_lob.append(caculate_array, '(');
       while j < column_number + 1 loop
         if alpine_miner_get_iaa_element(column_array_length_array, j) < 1 then
           dbms_lob.append(caculate_array,
                           '(alpine_miner_get_faa_element(y."k' || i || '",' || m ||
                           ')*log(2.0,(alpine_miner_get_faa_element(y."k' || i || '",' || m ||
                           ')*cast(1.0 as binary_double)/x."' ||
                           alpine_miner_get_v2aa_element(column_namearray,
                                                         j) || '")))');
           if countTotal != m then
             dbms_lob.append(caculate_array, '+');
           end if;
           m := m + 1;
         else
           l := 1;
           while l <=
                 alpine_miner_get_iaa_element(column_array_length_array, j) loop
             dbms_lob.append(caculate_array,
                             '(alpine_miner_get_faa_element(y."k' || i || '",' || m ||
                             ')*log(2.0,(alpine_miner_get_faa_element(y."k' || i || '",' || m ||
                             ')*cast(1.0 as binary_double)/alpine_miner_get_faa_element(x."' ||
                             alpine_miner_get_v2aa_element(column_namearray,
                                                           j) || '",' || l ||
                             '))))');
             if countTotal != m then
               dbms_lob.append(caculate_array, '+');
             end if;
             l := l + 1;
             m := m + 1;
           end loop;
         end if;
         if countTotal + 1 = m then
           dbms_lob.append(caculate_array, ') as d' || (i - 1));
           if i != k then
             dbms_lob.append(caculate_array, ',');
           end if;
         end if;
         j := j + 1;
       end loop;
       i := i + 1;
     end loop;
   elsif distancetype = 4 then
     i := 1;
     while i < (k + 1) loop
       m := 1;
       j := 1;
       dbms_lob.append(caculate_array, '(');
       while j < column_number + 1 loop
         if alpine_miner_get_iaa_element(column_array_length_array, j) < 1 then
           dbms_lob.append(caculate_array,
                           '(abs((x."' ||
                           alpine_miner_get_v2aa_element(column_namearray,
                                                         j) ||
                           '"*cast(1.0 as binary_double))-(alpine_miner_get_faa_element(y."k' || i || '",' || m ||
                           ')))/abs((x."' ||
                           alpine_miner_get_v2aa_element(column_namearray,
                                                         j) ||
                           '"*cast(1.0 as binary_double))+(alpine_miner_get_faa_element(y."k' || i || '",' || m ||
                           '))))');
           if countTotal != m then
             dbms_lob.append(caculate_array, '+');
           end if;
           m := m + 1;
         else
           l := 1;
           while l <=
                 alpine_miner_get_iaa_element(column_array_length_array, j) loop
             dbms_lob.append(caculate_array,
                             '(abs((alpine_miner_get_faa_element(x."' ||
                             alpine_miner_get_v2aa_element(column_namearray,
                                                           j) || '",' || l ||
                             ')*cast(1.0 as binary_double))-(alpine_miner_get_faa_element(y."k' || i || '",' || m ||
                             ')))/abs((alpine_miner_get_faa_element(x."' ||
                             alpine_miner_get_v2aa_element(column_namearray,
                                                           j) || '",' || l ||
                             ')*cast(1.0 as binary_double))+(alpine_miner_get_faa_element(y."k' || i || '",' || m ||
                             '))))');
             if countTotal != m then
               dbms_lob.append(caculate_array, '+');
             end if;
             l := l + 1;
             m := m + 1;
           end loop;
         end if;
         if countTotal + 1 = m then
           dbms_lob.append(caculate_array, ') as d' || (i - 1));
           if i != k then
             dbms_lob.append(caculate_array, ',');
           end if;
         end if;
         j := j + 1;
       end loop;
       i := i + 1;
     end loop;
   elsif distancetype = 5 then
     i := 1;
     while i < (k + 1) loop
       m := 1;
       j := 1;
       dbms_lob.append(caculate_array, '(');
       while j < column_number + 1 loop
         if alpine_miner_get_iaa_element(column_array_length_array, j) < 1 then
           dbms_lob.append(caculate_array,
                           'abs((x."' ||
                           alpine_miner_get_v2aa_element(column_namearray,
                                                         j) ||
                           '"*cast(1.0 as binary_double))-(alpine_miner_get_faa_element(y."k' || i || '",' || m ||
                           ')))');
           if countTotal != m then
             dbms_lob.append(caculate_array, '+');
           end if;
           m := m + 1;
         else
           l := 1;
           while l <=
                 alpine_miner_get_iaa_element(column_array_length_array, j) loop
             dbms_lob.append(caculate_array,
                             'abs((alpine_miner_get_faa_element(x."' ||
                             alpine_miner_get_v2aa_element(column_namearray,
                                                           j) || '",' || l ||
                             ')*cast(1.0 as binary_double))-(alpine_miner_get_faa_element(y."k' || i || '",' || m ||
                             ')))');
             if countTotal != m then
               dbms_lob.append(caculate_array, '+');
             end if;
             l := l + 1;
             m := m + 1;
           end loop;
         end if;
         if countTotal + 1 = m then
           dbms_lob.append(caculate_array, ') as d' || (i - 1));
           if i != k then
             dbms_lob.append(caculate_array, ',');
           end if;
         end if;
         j := j + 1;
       end loop;
       i := i + 1;
     end loop;
   elsif distancetype = 6 then
     i := 1;
     while i < (k + 1) loop
       j     := 1;
       m     := 1;
       temp1 := '(';
       temp2 := '(';
       temp3 := '(';
       while j < column_number + 1 loop
         if alpine_miner_get_iaa_element(column_array_length_array, j) < 1 then
           temp1 := temp1 || '(x."' ||
                    alpine_miner_get_v2aa_element(column_namearray, j) ||
                    '"*cast(1.0 as binary_double)*alpine_miner_get_faa_element(y."k' || i || '",' || m || '))';
           temp2 := temp2 || '(x."' ||
                    alpine_miner_get_v2aa_element(column_namearray, j) ||
                    '"*cast(1.0 as binary_double)*x."' ||
                    alpine_miner_get_v2aa_element(column_namearray, j) || '")';
           temp3 := temp3 || '(alpine_miner_get_faa_element(y."k' || i || '",' || m ||
                    ')*cast(1.0 as binary_double)*alpine_miner_get_faa_element(y."k' || i || '",' || m || '))';
           if countTotal != m then
             temp1 := temp1 || '+';
             temp2 := temp2 || '+';
             temp3 := temp3 || '+';
           end if;
           m := m + 1;
         else
           l := 1;
           while l <=
                 alpine_miner_get_iaa_element(column_array_length_array, j) loop
             temp1 := temp1 || '(alpine_miner_get_faa_element(x."' ||
                      alpine_miner_get_v2aa_element(column_namearray, j) || '",' || l ||
                      ')*cast(1.0 as binary_double)*alpine_miner_get_faa_element(y."k' || i || '",' || m || '))';
             temp2 := temp2 || '(alpine_miner_get_faa_element(x."' ||
                      alpine_miner_get_v2aa_element(column_namearray, j) || '",' || l ||
                      ')*cast(1.0 as binary_double)*alpine_miner_get_faa_element(x."' ||
                      alpine_miner_get_v2aa_element(column_namearray, j) || '",' || l || '))';
             temp3 := temp3 || '(alpine_miner_get_faa_element(y."k' || i || '",' || m ||
                      ')*cast(1.0 as binary_double)*alpine_miner_get_faa_element(y."k' || i || '",' || m || '))';
             if countTotal != m then
               temp1 := temp1 || '+';
               temp2 := temp2 || '+';
               temp3 := temp3 || '+';
             end if;
             l := l + 1;
             m := m + 1;
           end loop;
         end if;
         if countTotal + 1 = m then
           temp1 := temp1 || ')';
           temp2 := temp2 || ')';
           temp3 := temp3 || ')';
         end if;
         j := j + 1;
       end loop;
       if i = k then
         temp4 := 'acos(case when (' || temp1 || '/(sqrt(' || temp2 ||
                  ')*sqrt(' || temp3 || ')))>1 then 1 when (' || temp1 ||
                  '/(sqrt(' || temp2 || ')*sqrt(' || temp3 ||
                  ')))<-1 then -1 else (' || temp1 || '/(sqrt(' || temp2 ||
                  ')*sqrt(' || temp3 || '))) end ) as d' || (i - 1);
       else
         temp4 := 'acos(case when (' || temp1 || '/(sqrt(' || temp2 ||
                  ')*sqrt(' || temp3 || ')))>1 then 1 when (' || temp1 ||
                  '/(sqrt(' || temp2 || ')*sqrt(' || temp3 ||
                  ')))<-1 then -1 else (' || temp1 || '/(sqrt(' || temp2 ||
                  ')*sqrt(' || temp3 || '))) end ) as d' || (i - 1) || ',';
       end if;
       dbms_lob.append(caculate_array, temp4);
       i := i + 1;
     end loop;
   elsif distancetype = 7 then
     i := 1;
     while i < (k + 1) loop
       j     := 1;
       m     := 1;
       temp1 := '(';
       temp2 := '(';
       temp3 := '(';
       while j < column_number + 1 loop
         if alpine_miner_get_iaa_element(column_array_length_array, j) < 1 then
           temp1 := temp1 || '(x."' ||
                    alpine_miner_get_v2aa_element(column_namearray, j) ||
                    '"*cast(1.0 as binary_double))';
           temp2 := temp2 || '(alpine_miner_get_faa_element(y."k' || i || '",' || m || '))';
           temp3 := temp3 || '(x."' ||
                    alpine_miner_get_v2aa_element(column_namearray, j) ||
                    '"*cast(1.0 as binary_double)*alpine_miner_get_faa_element(y."k' || i || '",' || m || '))';
           if countTotal != m then
             temp1 := temp1 || '+';
             temp2 := temp2 || '+';
             temp3 := temp3 || '+';
           end if;
           m := m + 1;
         else
           l := 1;
           while l <=
                 alpine_miner_get_iaa_element(column_array_length_array, j) loop
             temp1 := temp1 || '(alpine_miner_get_faa_element(x."' ||
                      alpine_miner_get_v2aa_element(column_namearray, j) || '",' || l ||
                      ')*cast(1.0 as binary_double))';
             temp2 := temp2 || '(alpine_miner_get_faa_element(y."k' || i || '",' || m || '))';
             temp3 := temp3 || '(alpine_miner_get_faa_element(x."' ||
                      alpine_miner_get_v2aa_element(column_namearray, j) || '",' || l ||
                      ')*cast(1.0 as binary_double)*alpine_miner_get_faa_element(y."k' || i || '",' || m || '))';
             if countTotal != m then
               temp1 := temp1 || '+';
               temp2 := temp2 || '+';
               temp3 := temp3 || '+';
             end if;
             l := l + 1;
             m := m + 1;
           end loop;
         end if;
         if countTotal + 1 = m then
           temp1 := temp1 || ')';
           temp2 := temp2 || ')';
           temp3 := temp3 || ')';
         end if;
         j := j + 1;
       end loop;
       if i = k then
         temp4 := '(-2*' || temp3 || '/(' || temp1 || '+' || temp2 ||
                  ')) as d' || (i - 1);
       else
         temp4 := '(-2*' || temp3 || '/(' || temp1 || '+' || temp2 ||
                  ')) as d' || (i - 1) || ',';
       end if;
       dbms_lob.append(caculate_array, temp4);
       i := i + 1;
     end loop;
   elsif distancetype = 8 then
     i := 1;
     while i < (k + 1) loop
       m := 1;
       j := 1;
       dbms_lob.append(caculate_array, '-(');
       while j < column_number + 1 loop
         if alpine_miner_get_iaa_element(column_array_length_array, j) < 1 then
           dbms_lob.append(caculate_array,
                           '(x."' ||
                           alpine_miner_get_v2aa_element(column_namearray,
                                                         j) ||
                           '"*cast(1.0 as binary_double)*alpine_miner_get_faa_element(y."k' || i || '",' || m || '))');
           if countTotal != m then
             dbms_lob.append(caculate_array, '+');
           end if;
           m := m + 1;
         else
           l := 1;
           while l <=
                 alpine_miner_get_iaa_element(column_array_length_array, j) loop
             dbms_lob.append(caculate_array,
                             '(alpine_miner_get_faa_element(x."' ||
                             alpine_miner_get_v2aa_element(column_namearray,
                                                           j) || '",' || l ||
                             ')*cast(1.0 as binary_double)*alpine_miner_get_faa_element(y."k' || i || '",' || m || '))');
             if countTotal != m then
               dbms_lob.append(caculate_array, '+');
             end if;
             l := l + 1;
             m := m + 1;
           end loop;
         end if;
         if countTotal + 1 = m then
           dbms_lob.append(caculate_array, ') as d' || (i - 1));
           if i != k then
             dbms_lob.append(caculate_array, ',');
           end if;
         end if;
         j := j + 1;
       end loop;
       i := i + 1;
     end loop;
   elsif distancetype = 9 then
     i := 1;
     while i < (k + 1) loop
       j     := 1;
       m     := 1;
       temp1 := '(';
       temp2 := '(';
       temp3 := '(';
       while j < column_number + 1 loop
         if alpine_miner_get_iaa_element(column_array_length_array, j) < 1 then
           temp1 := temp1 || '(x."' ||
                    alpine_miner_get_v2aa_element(column_namearray, j) ||
                    '"*cast(1.0 as binary_double))';
           temp2 := temp2 || '(alpine_miner_get_faa_element(y."k' || i || '",' || m || '))';
           temp3 := temp3 || '(x."' ||
                    alpine_miner_get_v2aa_element(column_namearray, j) ||
                    '"*cast(1.0 as binary_double)*alpine_miner_get_faa_element(y."k' || i || '",' || m || '))';
           if countTotal != m then
             temp1 := temp1 || '+';
             temp2 := temp2 || '+';
             temp3 := temp3 || '+';
           end if;
           m := m + 1;
         else
           l := 1;
           while l <=
                 alpine_miner_get_iaa_element(column_array_length_array, j) loop
             temp1 := temp1 || '(alpine_miner_get_faa_element(x."' ||
                      alpine_miner_get_v2aa_element(column_namearray, j) || '",' || l ||
                      ')*cast(1.0 as binary_double))';
             temp2 := temp2 || '(alpine_miner_get_faa_element(y."k' || i || '",' || m || '))';
             temp3 := temp3 || '(alpine_miner_get_faa_element(x."' ||
                      alpine_miner_get_v2aa_element(column_namearray, j) || '",' || l ||
                      ')*cast(1.0 as binary_double)*alpine_miner_get_faa_element(y."k' || i || '",' || m || '))';
             if countTotal != m then
               temp1 := temp1 || '+';
               temp2 := temp2 || '+';
               temp3 := temp3 || '+';
             end if;
             l := l + 1;
             m := m + 1;
           end loop;
         end if;
         if countTotal + 1 = m then
           temp1 := temp1 || ')';
           temp2 := temp2 || ')';
           temp3 := temp3 || ')';
         end if;
         j := j + 1;
       end loop;
       if i = k then
         temp4 := '(-' || temp3 || '/(' || temp1 || '+' || temp2 || '-' ||
                  temp3 || ')) as d' || (i - 1);
       else
         temp4 := '(-' || temp3 || '/(' || temp1 || '+' || temp2 || '-' ||
                  temp3 || ')) as d' || (i - 1) || ',';
       end if;
       dbms_lob.append(caculate_array, temp4);
       i := i + 1;
     end loop;
   end if;
   return(caculate_array);
 end alpine_miner_kmeans_distance1;
/



CREATE OR REPLACE FUNCTION ALPINE_MINER_KMEANS_C_1_5(table_name               varchar2,
                                                     table_name_withoutschema varchar2,
                                                     column_name              varchar2array,
                                                     column_number            integer,
                                                     id                       varchar2,
                                                     tempid                   varchar2,
                                                     clustername              varchar2,
                                                     k                        integer,
                                                     max_run                  integer,
                                                     max_iter                 integer,
                                                     distance                 integer)
  return floatarray as
  temptablename   varchar2(30);
  executesql      clob := ' ';
  executesql_temp clob := ' ';
  i               integer := 0;
  j               integer := 0;
  column_array    clob := ' ';
  column_all      clob := ' ';
  run             integer := 1;
  none_stable     integer;
  tmp_res_1       varchar2(30);
  tmp_res_2       varchar2(30);
  tmp_res_3       varchar2(30);
  tmp_res_4       varchar2(30);
  avg_array       clob := ' ';
  comp_sql        clob := ' ';
  x_array         clob := ' ';
  result1         varchar2(30);
  sampleid        integer := 0;
  d_array         clob := ' ';
  d_array1        clob := ' ';
  caculate_array  clob := ' ';
  columnname      clob := ' ';
  alpine_id       varchar2(30);
  resultarray     floatarray;
  nullflag        integer := 0;
  tempsum         binary_double;
  tempint         integer := 1;
  PRAGMA AUTONOMOUS_TRANSACTION;
begin
  temptablename := table_name_withoutschema;
  executesql    := 'call proc_droptemptableifexists(''' || temptablename ||
                   'copy'')';
  execute immediate executesql;
  executesql := 'alter session force parallel dml';
  execute immediate executesql;
  if id = 'null' then
    executesql := 'create table ' || temptablename ||
                  'copy parallel as(select ' || table_name ||
                  '.*,row_number() over (order by 1) ' || tempid ||
                  ' from ' || table_name || ' where ';
    alpine_id  := tempid;
  else
    executesql := 'create table ' || temptablename ||
                  'copy parallel as(select * from ' || table_name ||
                  ' where ';
    alpine_id  := id;
  end if;
  i := 1;
  while i < (column_number) loop
    dbms_lob.append(executesql,
                    ' "' || column_name(i) || '" is not null and ');
    i := i + 1;
  end loop;
  dbms_lob.append(executesql, ' "' || column_name(i) || '" is not null)');
  execute immediate executesql;
  executesql := 'alter session disable parallel dml';
  execute immediate executesql;
  i := 2;
  while i < (column_number + 1) loop
    column_array := column_array || ',"' || column_name(i) || '"';
    i            := i + 1;
  end loop;
  executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                '_random_new'')';
  execute immediate executesql;
  executesql := 'select tablek1.seq sample_id,0 as stable,';
  column_all := ' ';
  i          := 1;
  while i < (k + 1) loop
    j := 1;
    while j < (column_number + 1) loop
      dbms_lob.append(column_all,
                      'cast("k' || i || '' || column_name(j) ||
                      '" as binary_double) "k' || i || '' || column_name(j) || '",');
      j := j + 1;
    end loop;
    i := i + 1;
  end loop;
  dbms_lob.append(executesql, column_all || '0 as iter from ');
  i := 1;
  while i < (k + 1) loop
    dbms_lob.append(executesql, '(select ');
    j := 1;
    while j < (column_number + 1) loop
      dbms_lob.append(executesql,
                      '"' || column_name(j) || '" "k' || i ||
                      column_name(j) || '",');
      j := j + 1;
    end loop;
    if i = 1 then
      dbms_lob.append(executesql,
                      ' row_number() over (order by DBMS_RANDOM.VALUE())-1 as seq from (select ' ||
                      temptablename || 'copy.* from ' || temptablename ||
                      'copy order by DBMS_RANDOM.VALUE()) where rownum<' ||
                      max_run || '+1) tablek' || i || ' inner join ');
    else
      if i = k then
        dbms_lob.append(executesql,
                        ' row_number() over (order by DBMS_RANDOM.VALUE())-1 as seq from (select ' ||
                        temptablename || 'copy.* from ' || temptablename ||
                        'copy order by DBMS_RANDOM.VALUE()) where rownum<' ||
                        max_run || '+1) tablek' || i || ' on tablek' ||
                        (i - 1) || '.seq=tablek' || i || '.seq');
      else
        dbms_lob.append(executesql,
                        ' row_number() over (order by DBMS_RANDOM.VALUE())-1 as seq from (select  rownum alpine_no,' ||
                        temptablename || 'copy.* from ' || temptablename ||
                        'copy order by DBMS_RANDOM.VALUE() ) where rownum<' ||
                        max_run || '+1) tablek' || i || ' on tablek' ||
                        (i - 1) || '.seq=tablek' || i || '.seq inner join ');
      end if;
    end if;
    i := i + 1;
  end loop;
  executesql_temp := 'create table ';
  dbms_lob.append(executesql_temp,
                  temptablename || '_random_new parallel as (');
  dbms_lob.append(executesql_temp, executesql);
  dbms_lob.append(executesql_temp, ')');
  executesql := 'alter session force parallel dml';
  execute immediate executesql;
  execute immediate executesql_temp;
  executesql := 'alter session disable parallel dml';
  execute immediate executesql;
  while run <= max_iter loop
    tmp_res_1 := to_char('tmp_res_1' || run);
    tmp_res_2 := to_char('tmp_res_2' || run);
    tmp_res_3 := to_char('tmp_res_3' || run);
    tmp_res_4 := to_char('tmp_res_4' || run);
    i         := 2;
    avg_array := 'trunc(avg("' || column_name(1) || '"),10) "' ||
                 column_name(1) || '"';
    while i < (column_number + 1) loop
      avg_array := avg_array || ',trunc(avg("' || column_name(i) ||
                   '"),10) "' || column_name(i) || '"';
      i         := i + 1;
    end loop;
    i        := 0;
    j        := 0;
    d_array  := 'case ';
    d_array1 := 'case ';
    while i < k - 1 loop
      j        := i + 1;
      d_array  := d_array || ' when d' || i || '<=d' || j;
      d_array1 := d_array1 || ' when d' || i || '<=d' || j;
      j        := j + 1;
      while j < k loop
        d_array  := d_array || ' and d' || i || '<=d' || j;
        d_array1 := d_array1 || ' and d' || i || '<=d' || j;
        j        := j + 1;
      end loop;
      d_array  := d_array || ' then ' || i;
      d_array1 := d_array1 || ' then d' || i;
      i        := i + 1;
    end loop;
    d_array        := d_array || ' else ' || (k - 1) || ' end';
    d_array1       := d_array1 || ' else d' || (k - 1) || ' end';
    caculate_array := '';
    columnname     := 'varchar2array(';
    i              := 1;
    while i < column_number loop
      columnname := columnname || '''' || column_name(i) || ''',';
      i          := i + 1;
    end loop;
    columnname := columnname || '''' || column_name(i) || ''')';
    executesql := 'select alpine_miner_kmeans_distance(' || distance || ',' ||
                  columnname || ',' || column_number || ',' || k ||
                  ') from dual';
    execute immediate executesql
      into caculate_array; /*data_array:='varchar2array('; i:=1; while i<column_number loop data_array:=data_array||'"'||column_name(i)||'",'; i:=i+1; end loop; data_array:=data_array||'"'||column_name(i)||'")';*/
    executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                  tmp_res_2 || ''')';
    execute immediate executesql;
    executesql := 'alter session force parallel dml';
    execute immediate executesql;
    executesql := 'create table ' || temptablename || tmp_res_2 ||
                  ' parallel as (select sample_id,' || alpine_id || ', ' ||
                  d_array || ' as cluster_id from ( select sample_id,' ||
                  alpine_id || ', ' || caculate_array || ' from ' ||
                  temptablename || 'copy x inner join ' || temptablename ||
                  '_random_new y on y.stable=0) foo)';
    execute immediate executesql;
    executesql := 'alter session disable parallel dml';
    execute immediate executesql;
    executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                  tmp_res_1 || ''')';
    execute immediate executesql;
    executesql := 'alter session force parallel dml';
    execute immediate executesql;
    executesql := 'create table ' || temptablename || tmp_res_1 ||
                  ' parallel as ( select sample_id, cluster_id, ' ||
                  avg_array || ' from ' || temptablename || tmp_res_2 ||
                  ' x,' || temptablename || 'copy y where x.' || alpine_id ||
                  '=y.' || alpine_id || ' group by sample_id,cluster_id )';
    execute immediate executesql;
    executesql := 'alter session disable parallel dml';
    execute immediate executesql;
    comp_sql := '(case when ';
    i        := 1;
    while i < (k + 1) loop
      j := 1;
      while j < column_number + 1 loop
        if i = k and j = column_number then
          comp_sql := comp_sql || 'x."k' || i || column_name(j) || '"=y."k' || i ||
                      column_name(j) || '"';
        else
          comp_sql := comp_sql || 'x."k' || i || column_name(j) || '"=y."k' || i ||
                      column_name(j) || '" and ';
        end if;
        j := j + 1;
      end loop;
      i := i + 1;
    end loop;
    comp_sql   := comp_sql || ' then 1 else 0 end ) as stable';
    executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                  tmp_res_4 || ''')';
    execute immediate executesql;
    executesql := 'alter session force parallel dml';
    execute immediate executesql;
    executesql := 'select tablek1.sample_id,0 as stable,';
    column_all := '';
    i          := 1;
    while i < (k + 1) loop
      j := 1;
      while j < (column_number + 1) loop
        column_all := column_all || '"k' || i || column_name(j) || '",';
        j          := j + 1;
      end loop;
      i := i + 1;
    end loop;
    dbms_lob.append(executesql, column_all || ' 0 as iter from ');
    i := 1;
    while i < (k + 1) loop
      dbms_lob.append(executesql, '(select ');
      j := 1;
      while j < (column_number + 1) loop
        dbms_lob.append(executesql,
                        '"' || column_name(j) || '" "k' || i ||
                        column_name(j) || '",');
        j := j + 1;
      end loop;
      if i = 1 then
        dbms_lob.append(executesql,
                        ' sample_id from ' || temptablename || tmp_res_1 ||
                        ' where cluster_id=' || (i - 1) || ')  tablek' || i ||
                        ' inner join ');
      else
        if i = k then
          dbms_lob.append(executesql,
                          ' sample_id from ' || temptablename || tmp_res_1 ||
                          ' where cluster_id=' || (i - 1) || ')  tablek' || i ||
                          ' on tablek' || (i - 1) || '.sample_id=tablek' || i ||
                          '.sample_id');
        else
          dbms_lob.append(executesql,
                          ' sample_id from ' || temptablename || tmp_res_1 ||
                          ' where cluster_id=' || (i - 1) || ')  tablek' || i ||
                          ' on tablek' || (i - 1) || '.sample_id=tablek' || i ||
                          '.sample_id inner join ');
        end if;
      end if;
      i := i + 1;
    end loop;
    executesql := 'create table ' || temptablename || tmp_res_4 ||
                  ' parallel as (' || executesql || ')';
    execute immediate executesql;
    executesql := 'alter session disable parallel dml';
    execute immediate executesql;
    x_array := '';
    i       := 1;
    while i < (k + 1) loop
      j := 1;
      while j < (column_number + 1) loop
        x_array := x_array || 'x."k' || i || column_name(j) || '",';
        j       := j + 1;
      end loop;
      i := i + 1;
    end loop;
    executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                  tmp_res_3 || ''')';
    execute immediate executesql;
    executesql := 'alter session force parallel dml';
    execute immediate executesql;
    executesql := 'create table ' || temptablename || tmp_res_3 ||
                  ' parallel as ( select x.sample_id, ' || comp_sql || ',' ||
                  x_array || run || ' as iter from  ' || temptablename ||
                  tmp_res_4 || ' x, ' || temptablename ||
                  '_random_new  y where x.sample_id=y.sample_id  ) ';
    execute immediate executesql;
    executesql := 'alter session disable parallel dml';
    execute immediate executesql;
    executesql := 'insert into ' || temptablename || tmp_res_3 ||
                  ' (select a.* from ' || temptablename ||
                  '_random_new a left join ' || temptablename || tmp_res_3 ||
                  ' b on a.sample_id=b.sample_id';
    dbms_lob.append(executesql, ' where b.sample_id is null)');
    execute immediate executesql;
    executesql := 'truncate table ' || temptablename || '_random_new';
    execute immediate executesql;
    executesql := 'drop table ' || temptablename || '_random_new';
    execute immediate executesql;
    executesql := 'alter table ' || temptablename || tmp_res_3 ||
                  ' rename to ' || temptablename || '_random_new';
    execute immediate executesql;
    execute immediate 'select count(*)  from  ' || temptablename ||
                      '_random_new where stable=0'
      into none_stable;
    if none_stable = 0 then
      exit;
    end if;
    run := run + 1;
  end loop;
  i := 1;
  while i < (run + 1) loop
    tmp_res_1  := to_char('tmp_res_1' || i);
    executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                  tmp_res_1 || ''')';
    execute immediate executesql;
    tmp_res_2  := to_char('tmp_res_2' || i);
    executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                  tmp_res_2 || ''')';
    execute immediate executesql;
    tmp_res_3  := to_char('tmp_res_3' || i);
    executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                  tmp_res_3 || ''')';
    execute immediate executesql;
    tmp_res_4  := to_char('tmp_res_4' || i);
    executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                  tmp_res_4 || ''')';
    execute immediate executesql;
    i := i + 1;
  end loop;
  executesql := 'select floatarray(sample_id,len) from ( select sample_id,len,row_number() over(order by len) as seq from ( select sample_id,avg(len) as len from ( select sample_id,' ||
                alpine_id || ', ' || d_array1 ||
                ' as len from ( select sample_id, ' || alpine_id || ', ' ||
                caculate_array || ' from ' || temptablename ||
                'copy x inner join ' || temptablename ||
                '_random_new y on y.stable=1 )t )a group by sample_id )b )z where seq=1';
  execute immediate 'select count(*) from ' || temptablename ||
                    '_random_new y where y.stable=1'
    into tempint;
  if tempint <> 0 then
    execute immediate executesql
      into resultarray;
    sampleid := resultarray(1);
  else
    execute immediate 'select floatarray(0,0) from dual '
      into resultarray;
  end if;
  if sampleid is null then
    sampleid := 0;
    nullflag := 1;
  end if;
  result1    := 'result1';
  executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                result1 || ''')';
  execute immediate executesql;
  executesql := 'alter session force parallel dml';
  execute immediate executesql;
  execute immediate 'create table ' || temptablename || result1 ||
                    ' parallel as ( select * from  ' || temptablename ||
                    '_random_new  where sample_id =' || sampleid || ' )';
  executesql := 'alter session disable parallel dml';
  execute immediate executesql;
  if nullflag = 1 then
    executesql := 'select len from ( select sample_id,len,row_number() over(order by len) as seq from ( select sample_id,avg(len) as len from ( select sample_id,' ||
                  alpine_id || ', ' || d_array1 ||
                  ' as len from ( select sample_id, ' || alpine_id || ', ' ||
                  caculate_array || ' from ' || temptablename ||
                  'copy x inner join ' || temptablename ||
                  '_random_new y on y.stable=0 )t )a group by sample_id )b )z where seq=1';
    execute immediate executesql
      into tempsum;
    resultarray(2) := tempsum;
  end if;
  executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                'result2'')';
  execute immediate executesql;
  executesql := 'alter session force parallel dml';
  execute immediate executesql;
  execute immediate 'create table ' || temptablename ||
                    'result2 parallel as select ' || temptablename ||
                    'copy.*,0 ' || temptablename || 'copy_flag from ' ||
                    temptablename || 'copy';
  executesql := 'alter session disable parallel dml';
  execute immediate executesql;
  executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                'table_name_temp'')';
  execute immediate executesql;
  executesql := 'alter session force parallel dml';
  execute immediate executesql;
  executesql := 'select ' || alpine_id || ' as temp_id,' || d_array ||
                ' as ' || clustername || ' from ( select x.' || alpine_id || ',' ||
                caculate_array || ' from ' || temptablename ||
                'result2 x inner join ' || temptablename || result1 ||
                ' y on x.' || temptablename || 'copy_flag=0 ) foo ';
  execute immediate ' create table ' || temptablename ||
                    'table_name_temp parallel as ( ' || executesql || ' )';
  executesql := 'alter session disable parallel dml';
  execute immediate executesql;
  resultarray(1) := run;
  RETURN resultarray;
end alpine_miner_kmeans_c_1_5;
/

CREATE OR REPLACE FUNCTION ALPINE_MINER_KMEANS_C_1_5_2(table_name                varchar2,
                                                       table_name_withoutschema  varchar2,
                                                       column_namearray          varchar2ArrayArray,
                                                       column_array_length_array integerArrayArray,
                                                       column_number             integer,
                                                       id                        varchar2,
                                                       tempid                    varchar2,
                                                       clustername               varchar2,
                                                       k                         integer,
                                                       max_run                   integer,
                                                       max_iter                  integer,
                                                       distance                  integer)
  return floatarray as
  temptablename   varchar2(30);
  executesql      clob;
  executesql_temp clob;
  i               integer := 0;
  j               integer := 0;
  column_array    clob;
  column_all      clob;
  run             integer := 1;
  none_stable     integer;
  tmp_res_1       varchar2(30);
  tmp_res_2       varchar2(30);
  tmp_res_3       varchar2(30);
  tmp_res_4       varchar2(30);
  comp_sql        clob;
  x_array         clob;
  result1         varchar2(30);
  sampleid        integer := 0;
  d_array         clob;
  d_array1        clob;
  caculate_array  clob;
  alpine_id       varchar2(30);
  resultarray     floatarray;
  nullflag        integer := 0;
  tempsum         binary_double;
  tempint         integer := 1;
  countTotal      integer := 0;
  i_array         integerarray;
  stringlength    integer := 0;
  PRAGMA AUTONOMOUS_TRANSACTION;
begin
  for i in 1 .. column_array_length_array.count() loop
    i_array    := column_array_length_array(i);
    countTotal := countTotal + i_array.count();
  end loop;
  temptablename := table_name_withoutschema;
  executesql    := 'call proc_droptemptableifexists(''' || temptablename ||
                   'copy'')';
  execute immediate executesql;
  executesql := 'alter session force parallel dml';
  execute immediate executesql;
  if id = 'null' then
    executesql := 'create table ' || temptablename ||
                  'copy parallel as(select ' || table_name ||
                  '.*,row_number() over (order by 1) ' || tempid ||
                  ' from ' || table_name || ' where ';
    alpine_id  := tempid;
  else
    executesql := 'create table ' || temptablename ||
                  'copy parallel as(select * from ' || table_name ||
                  ' where ';
    alpine_id  := id;
  end if;
  i := 1;
  while i < (column_number) loop
    dbms_lob.append(executesql,
                    ' "' ||
                    alpine_miner_get_v2aa_element(column_namearray, i) ||
                    '" is not null and ');
    i := i + 1;
  end loop;
  dbms_lob.append(executesql,
                  ' "' ||
                  alpine_miner_get_v2aa_element(column_namearray, i) ||
                  '" is not null)');
  execute immediate executesql;
  executesql := 'alter session disable parallel dml';
  execute immediate executesql;
  i := 2;
  while i < (column_number + 1) loop
    column_array := column_array || ',"' ||
                    alpine_miner_get_v2aa_element(column_namearray, i) || '"';
    i            := i + 1;
  end loop;
  executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                '_random_new'')';
  execute immediate executesql;
  executesql := 'select tablek1.seq sample_id,0 as stable,';
  column_all := '';
  i          := 1;
  while i < (k + 1) loop
    column_all := column_all || ' "k' || i || '",';
    i          := i + 1;
  end loop;
  dbms_lob.append(executesql, column_all || '0 as iter from ');
  i := 1;
  while i < (k + 1) loop
    dbms_lob.append(executesql, '(select ');
    dbms_lob.append(executesql,
                    alpine_miner_v2aa2faa(column_namearray,
                                          column_array_length_array) ||
                    ' "k' || i || '",');
    if i = 1 then
      dbms_lob.append(executesql,
                      ' row_number() over (order by DBMS_RANDOM.VALUE)-1 as seq from (select  rownum alpine_no,' ||
                      temptablename || 'copy.* from ' || temptablename ||
                      'copy order by DBMS_RANDOM.VALUE() ) where rownum<' ||
                      max_run || '+1) tablek' || i || ' inner join ');
    else
      if i = k then
        dbms_lob.append(executesql,
                        ' row_number() over (order by DBMS_RANDOM.VALUE())-1 as seq from (select ' ||
                        temptablename || 'copy.* from ' || temptablename ||
                        'copy order by DBMS_RANDOM.VALUE()) where rownum<' ||
                        max_run || '+1) tablek' || i || ' on tablek' ||
                        (i - 1) || '.seq=tablek' || i || '.seq');
      else
        dbms_lob.append(executesql,
                        ' row_number() over (order by DBMS_RANDOM.VALUE())-1 as seq from (select  rownum alpine_no,' ||
                        temptablename || 'copy.* from ' || temptablename ||
                        'copy order by DBMS_RANDOM.VALUE() ) where rownum<' ||
                        max_run || '+1) tablek' || i || ' on tablek' ||
                        (i - 1) || '.seq=tablek' || i || '.seq inner join ');
      end if;
    end if;
    i := i + 1;
  end loop;
  executesql_temp := 'create table ';
  dbms_lob.append(executesql_temp,
                  temptablename || '_random_new parallel as (');
  dbms_lob.append(executesql_temp, executesql);
  dbms_lob.append(executesql_temp, ')');
  executesql := 'alter session force parallel dml';
  execute immediate executesql;
  execute immediate executesql_temp;
  executesql := 'alter session disable parallel dml';
  execute immediate executesql;
  while run <= max_iter loop
    tmp_res_1 := to_char('tmp_res_1' || run);
    tmp_res_2 := to_char('tmp_res_2' || run);
    tmp_res_3 := to_char('tmp_res_3' || run);
    tmp_res_4 := to_char('tmp_res_4' || run);
    i         := 0;
    j         := 0;
    d_array   := 'case ';
    d_array1  := 'case ';
    while i < k - 1 loop
      j        := i + 1;
      d_array  := d_array || ' when d' || i || '<=d' || j;
      d_array1 := d_array1 || ' when d' || i || '<=d' || j;
      j        := j + 1;
      while j < k loop
        d_array  := d_array || ' and d' || i || '<=d' || j;
        d_array1 := d_array1 || ' and d' || i || '<=d' || j;
        j        := j + 1;
      end loop;
      d_array  := d_array || ' then ' || i;
      d_array1 := d_array1 || ' then d' || i;
      i        := i + 1;
    end loop;
    d_array        := d_array || ' else ' || (k - 1) || ' end';
    d_array1       := d_array1 || ' else d' || (k - 1) || ' end';
    caculate_array := '';
    select alpine_miner_kmeans_distance1(distance,
                                         column_namearray,
                                         column_array_length_array,
                                         column_number,
                                         k)
      into caculate_array
      from dual;
    executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                  tmp_res_2 || ''')';
    execute immediate executesql;
    executesql := 'create table ' || temptablename || tmp_res_2 ||
                  ' parallel as (select sample_id,' || alpine_id || ', ' ||
                  d_array || ' as cluster_id from ( select sample_id,' ||
                  alpine_id || ', ' || caculate_array || ' from ' ||
                  temptablename || 'copy x inner join ' || temptablename ||
                  '_random_new y on y.stable=0) foo)';
    execute immediate executesql;
    executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                  tmp_res_1 || ''')';
    execute immediate executesql;
    executesql := 'alter session force parallel dml';
    execute immediate executesql;
    executesql := 'create table ' || temptablename || tmp_res_1 ||
                  ' parallel as ( select sample_id, cluster_id, ' ||
                  alpine_miner_v2aa2faaavg(column_namearray,
                                           column_array_length_array) ||
                  ' avg from ' || temptablename || tmp_res_2 || ' x,' ||
                  temptablename || 'copy y where x.' || alpine_id || '=y.' ||
                  alpine_id || ' group by sample_id,cluster_id )';
    execute immediate executesql;
    executesql := 'alter session disable parallel dml';
    execute immediate executesql;
    comp_sql := '(case when ';
    i        := 1;
    while i < (k + 1) loop
      j := 1;
      while j < countTotal + 1 loop
        if i = k and j = column_number then
          comp_sql := comp_sql || 'alpine_miner_get_faa_element(x."k' || i || '",' || j ||
                      ')=alpine_miner_get_faa_element(y."k' || i || '",' || j || ')';
        else
          comp_sql := comp_sql || 'alpine_miner_get_faa_element(x."k' || i || '",' || j ||
                      ')=alpine_miner_get_faa_element(y."k' || i || '",' || j ||
                      ') and ';
        end if;
        j := j + 1;
      end loop;
      i := i + 1;
    end loop;
    comp_sql   := comp_sql || ' then 1 else 0 end ) as stable';
    executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                  tmp_res_4 || ''')';
    execute immediate executesql;
    executesql := 'alter session force parallel dml';
    execute immediate executesql;
    executesql := 'select tablek1.sample_id,0 as stable,';
    i          := 1;
    while i < (k + 1) loop
      dbms_lob.append(executesql, '"k' || i || '",');
      i := i + 1;
    end loop;
    dbms_lob.append(executesql, ' 0 as iter from ');
    i := 1;
    while i < (k + 1) loop
      dbms_lob.append(executesql, '(select avg "k' || i || '",');
      if i = 1 then
        dbms_lob.append(executesql,
                        ' sample_id from ' || temptablename || tmp_res_1 ||
                        ' where cluster_id=' || (i - 1) || ')  tablek' || i ||
                        ' inner join ');
      else
        if i = k then
          dbms_lob.append(executesql,
                          ' sample_id from ' || temptablename || tmp_res_1 ||
                          ' where cluster_id=' || (i - 1) || ')  tablek' || i ||
                          ' on tablek' || (i - 1) || '.sample_id=tablek' || i ||
                          '.sample_id');
        else
          dbms_lob.append(executesql,
                          ' sample_id from ' || temptablename || tmp_res_1 ||
                          ' where cluster_id=' || (i - 1) || ')  tablek' || i ||
                          ' on tablek' || (i - 1) || '.sample_id=tablek' || i ||
                          '.sample_id inner join ');
        end if;
      end if;
      i := i + 1;
    end loop;
    executesql := 'create table ' || temptablename || tmp_res_4 ||
                  ' parallel as (' || executesql || ')';
    execute immediate executesql;
    executesql := 'alter session disable parallel dml';
    execute immediate executesql;
    x_array := '';
    i       := 1;
    while i < (k + 1) loop
      x_array := x_array || 'x."k' || i || '",';
      i       := i + 1;
    end loop;
    executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                  tmp_res_3 || ''')';
    execute immediate executesql;
    executesql := 'alter session force parallel dml';
    execute immediate executesql;
    executesql := 'create table ' || temptablename || tmp_res_3 ||
                  ' parallel as ( select x.sample_id, ' || comp_sql || ',' ||
                  x_array || run || ' as iter from  ' || temptablename ||
                  tmp_res_4 || ' x, ' || temptablename ||
                  '_random_new  y where x.sample_id=y.sample_id  ) ';
    execute immediate executesql;
    executesql := 'alter session disable parallel dml';
    execute immediate executesql;
    executesql := 'insert into ' || temptablename || tmp_res_3 ||
                  ' (select a.* from ' || temptablename ||
                  '_random_new a left join ' || temptablename || tmp_res_3 ||
                  ' b on a.sample_id=b.sample_id';
    dbms_lob.append(executesql, ' where b.sample_id is null)');
    execute immediate executesql;
    executesql := 'truncate table ' || temptablename || '_random_new';
    execute immediate executesql;
    executesql := 'drop table ' || temptablename || '_random_new';
    execute immediate executesql;
    executesql := 'alter table ' || temptablename || tmp_res_3 ||
                  ' rename to ' || temptablename || '_random_new';
    execute immediate executesql;
    execute immediate 'select count(*)  from  ' || temptablename ||
                      '_random_new where stable=0'
      into none_stable;
    if none_stable = 0 then
      exit;
    end if;
    run := run + 1;
  end loop;
  
    -----drop temp table start--------------
 i:=1;
 while i < run loop
     tmp_res_1 := to_char('tmp_res_1' || i);
  executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                  tmp_res_1 || ''')';
    execute immediate executesql;
    tmp_res_2 := to_char('tmp_res_2' || i);
    executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                  tmp_res_2 || ''')';
    execute immediate executesql;
    tmp_res_3 := to_char('tmp_res_3' || i);
    executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                  tmp_res_3 || ''')';
    execute immediate executesql;
    tmp_res_4 := to_char('tmp_res_4' || i);
    executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                  tmp_res_4 || ''')';
    execute immediate executesql;
 i := i + 1;
end loop;
-----drop temp table end--------------

  executesql := 'select floatarray(sample_id,len) from ( select sample_id,len,row_number() over(order by len) as seq from ( select sample_id,avg(len) as len from ( select sample_id,' ||
                alpine_id || ', ' || d_array1 ||
                ' as len from ( select sample_id, ' || alpine_id || ', ' ||
                caculate_array || ' from ' || temptablename ||
                'copy x inner join ' || temptablename ||
                '_random_new y on y.stable=1 )t )a group by sample_id )b )z where seq=1';
  execute immediate 'select count(*) from ' || temptablename ||
                    '_random_new y where y.stable=1'
    into tempint;
  if tempint <> 0 then
    execute immediate executesql
      into resultarray;
    sampleid := resultarray(1);
  else
    execute immediate 'select floatarray(0,0) from dual '
      into resultarray;
  end if;
  if sampleid is null then
    sampleid := 0;
    nullflag := 1;
  end if;
  result1    := 'result1';
  executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                result1 || ''')';
  execute immediate executesql;
  executesql := 'alter session force parallel dml';
  execute immediate executesql;
  execute immediate 'create table ' || temptablename || result1 ||
                    ' parallel as ( select * from  ' || temptablename ||
                    '_random_new  where sample_id =' || sampleid || ' )';
  if nullflag = 1 then
    executesql := 'select len from ( select sample_id,len,row_number() over(order by len) as seq from ( select sample_id,avg(len) as len from ( select sample_id,' ||
                  alpine_id || ', ' || d_array1 ||
                  ' as len from ( select sample_id, ' || alpine_id || ', ' ||
                  caculate_array || ' from ' || temptablename ||
                  'copy x inner join ' || temptablename ||
                  '_random_new y on y.stable=0 )t )a group by sample_id )b )z where seq=1';
    execute immediate executesql
      into tempsum;
    resultarray(2) := tempsum;
  end if;
  executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                'result2'')';
  execute immediate executesql;
  execute immediate 'create table ' || temptablename ||
                    'result2 as select ' || temptablename || 'copy.*,0 ' ||
                    temptablename || 'copy_flag from ' || temptablename ||
                    'copy';
  executesql := 'call proc_droptemptableifexists(''' || temptablename ||
                'table_name_temp'')';
  execute immediate executesql;
  executesql := 'select ' || alpine_id || ' as temp_id,' || d_array ||
                ' as ' || clustername || ' from ( select x.' || alpine_id || ',' ||
                caculate_array || ' from ' || temptablename ||
                'result2 x inner join ' || temptablename || result1 ||
                ' y on x.' || temptablename || 'copy_flag=0 ) foo ';
  execute immediate ' create table ' || temptablename ||
                    'table_name_temp parallel as ( ' || executesql || ' )';
  resultarray(1) := run;
  executesql := 'alter session disable parallel dml';
  execute immediate executesql;
  RETURN resultarray;
end alpine_miner_kmeans_c_1_5_2;
/

