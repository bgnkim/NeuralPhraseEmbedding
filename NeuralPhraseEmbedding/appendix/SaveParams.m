clear;

fid = fopen('Words_normalized.json', 'wt');
load('vars.normalized.100.mat');
fprintf(fid,'{');
for i=1:size(We,2)
    fprintf(fid,'"%s":[',words{1,i});
    for j=1:size(We,1)
        fprintf(fid,'%f',We(j,i));
        if j < size(We,1)
            fprintf(fid, ',');
        end
    end
    display([num2str(i) ' of ' num2str(size(We,2))]);
    fprintf(fid,']');
    if i < size(We,2)
        fprintf(fid,',');
    end
end
fprintf(fid,'}');
fclose(fid);

clear;

fid = fopen('params.json', 'wt');
load('params.mat');

fprintf(fid,'{"W1":[');
for i=1:size(W1,1)
    fprintf(fid,'[');
    for j=1:size(W1,2)
        fprintf(fid,'%f',W1(i,j));
        if j < size(W1,2)
            fprintf(fid, ',');
        end
    end
    fprintf(fid,']');
    if i < size(W1,1)
        fprintf(fid,',');
    end
end
fprintf(fid,'],"W2":[');
for i=1:size(W2,1)
    fprintf(fid,'[');
    for j=1:size(W2,2)
        fprintf(fid,'%f',W2(i,j));
        if j < size(W2,2)
            fprintf(fid, ',');
        end
    end
    fprintf(fid,']');
    if i < size(W2,1)
        fprintf(fid,',');
    end
end
fprintf(fid,'],"b1":[');
for i=1:size(b1,1)
    fprintf(fid,'[');
    for j=1:size(b1,2)
        fprintf(fid,'%f',b1(i,j));
        if j < size(b1,2)
            fprintf(fid, ',');
        end
    end
    fprintf(fid,']');
    if i < size(b1,1)
        fprintf(fid,',');
    end
end
fprintf(fid,']}');
fclose(fid);