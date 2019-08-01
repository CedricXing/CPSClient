import os

def main():
    sum_time = 100
    success_time = 0
    command_sh = '/home/cedric/Downloads/jdk-12.0.1/bin/java -javaagent:/home/cedric/Downloads/idea-IU-191.7479.19/lib/idea_rt.jar=35349:/home/cedric/Downloads/idea-IU-191.7479.19/bin -Dfile.encoding=UTF-8 -classpath /home/cedric/Downloads/fel.jar:/home/cedric/Downloads/jsch-0.1.54.jar:/home/cedric/Desktop/OptimalControl/out/production/OptimalControl MPC.Automata'
    while success_time < sum_time:
        code = os.system('timeout 10m ' + command_sh)
        if code == 0: ### success
            success_time += 1
    	    with open('current_id.txt','w') as f:
		f.write(success_time)

if __name__ == '__main__':
    main()
