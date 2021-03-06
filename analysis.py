from matplotlib.pyplot import show, savefig, title, ylim, ylabel
from pandas import read_csv

locations = ['Bathroom','Desk','Window','Hallway','Bed']

df = read_csv('data.csv', quotechar="'")

for l in locations:
        
    b = df[df['location'] == l]
    b.boxplot(column="level",by='ssid', rot='-90')
    ylim(-90,-30)
    title(l)
    ylabel('dB')
    savefig(f'plot-{l}.png')

asus = df[df['ssid'] == 'ASUS_5G' ]
asus.boxplot(column="level",by='location', rot='-90')
ylim(-90,-30)
title('ASUS_5G')
ylabel('dB')
savefig('asus.svg')

h = df[df['ssid'] == 'ASUS_5G']
print(h.groupby('location').size())

df['timestamp'] //=  1000_0000
print(df) 
k = df.pivot_table(values='level', index=df.index, columns='ssid', aggfunc='first')
print(k)
