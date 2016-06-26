#!/bin/bash
for i in $(seq 1 100); do 
java -jar dist/BURLAPController_Snort.jar 
grep -a2 -b2 -i close output/vi.episode
done
