/*
 * Copyright (c) 2013 LDBC
 * Linked Data Benchmark Council (http://ldbc.eu)
 *
 * This file is part of ldbc_socialnet_dbgen.
 *
 * ldbc_socialnet_dbgen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ldbc_socialnet_dbgen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ldbc_socialnet_dbgen.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2011 OpenLink Software <bdsmt@openlinksw.com>
 * All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation;  only Version 2 of the License dated
 * June 1991.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package ldbc.socialnet.dbgen.generator;

import java.util.TreeSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import ldbc.socialnet.dbgen.dictionary.LocationDictionary;
import ldbc.socialnet.dbgen.dictionary.TagDictionary;
import ldbc.socialnet.dbgen.objects.Friend;
import ldbc.socialnet.dbgen.objects.Group;
import ldbc.socialnet.dbgen.objects.GroupMemberShip;
import ldbc.socialnet.dbgen.objects.ReducedUserProfile;
import ldbc.socialnet.dbgen.objects.UserExtraInfo;
import ldbc.socialnet.dbgen.util.RandomGeneratorFarm;
import ldbc.socialnet.dbgen.vocabulary.SN;


public class GroupGenerator {
	DateGenerator dateGenerator;
	LocationDictionary locationDic;
	TagDictionary tagDic;
    long deltaTime;

	public GroupGenerator(DateGenerator dateGenerator, LocationDictionary locationDic, 
			TagDictionary tagDic, long deltaTime ){
		this.dateGenerator = dateGenerator; 
		this.locationDic = locationDic; 
		this.tagDic = tagDic;
        this.deltaTime = deltaTime;
	}

	public Group createGroup(RandomGeneratorFarm randomFarm, long groupId, ReducedUserProfile user){
        long date = dateGenerator.randomDate(randomFarm.get(RandomGeneratorFarm.Aspect.DATE), user.getCreationDate() + deltaTime);
        if( date > dateGenerator.getEndDateTime() )  return null;
		Group group = new Group();

		group.setGroupId(SN.composeId(groupId,date));
		group.setModeratorId(user.getAccountId());
		group.setCreatedDate(date);

		//Use the user location for group locationIdx
		group.setLocationIdx(user.getLocationId());
				
		TreeSet<Integer> tagSet = user.getSetOfTags();
		Iterator<Integer> iter = tagSet.iterator();
        int idx = randomFarm.get(RandomGeneratorFarm.Aspect.GROUP_INTEREST).nextInt(tagSet.size());
        for (int i = 0; i < idx; i++){
            iter.next();
        }
		  
		int interestIdx = iter.next().intValue();
		
		//Set tags of this group
		Integer tags[] = new Integer[1];
		tags[0] = interestIdx;
		
		//Set name of group
		group.setGroupName("Group for " + tagDic.getName(interestIdx).replace("\"","\\\"") + " in " + locationDic.getLocationName(group.getLocationIdx()));
		
		group.setTags(tags);
		
		return group; 
	}
	
	public Group createAlbum(RandomGeneratorFarm randomFarm, long groupId, ReducedUserProfile user, UserExtraInfo extraInfo, int numAlbum, double memberProb) {
	    Group group = createGroup(randomFarm, groupId,user);
        if( group == null ) return null;
	    Vector<Integer> countries = locationDic.getCountries();
	    int randomCountry = randomFarm.get(RandomGeneratorFarm.Aspect.COUNTRY).nextInt(countries.size());
	    group.setLocationIdx(countries.get(randomCountry));
	    group.setGroupName("Album " + numAlbum + " of " + extraInfo.getFirstName() + " " + extraInfo.getLastName());
	    Friend[] friends = user.getFriendList();
	    group.initAllMemberships(user.getNumFriendsAdded());
	    for (int i = 0; i < user.getNumFriendsAdded(); i++) {
	        double randMemberProb = randomFarm.get(RandomGeneratorFarm.Aspect.ALBUM_MEMBERSHIP).nextDouble();
	        if (randMemberProb < memberProb) {
	            GroupMemberShip memberShip = createGroupMember(randomFarm.get(RandomGeneratorFarm.Aspect.MEMBERSHIP_INDEX),friends[i].getFriendAcc(),
	                    group.getCreatedDate(), friends[i]);
                if( memberShip != null ) {
                    memberShip.setGroupId(group.getGroupId());
                    memberShip.userCreationDate = friends[i].toCreationDate;
                    group.addMember(memberShip);
                }
	        }
	    }
	    return group;
	}
	
	public GroupMemberShip createGroupMember(Random random, long userId, long groupCreatedDate, Friend friend){
        long date = dateGenerator.randomDate(random,Math.max(groupCreatedDate, friend.getCreatedTime()+deltaTime));
        if( date > dateGenerator.getEndDateTime() ) return null;
		GroupMemberShip memberShip = new GroupMemberShip();
		memberShip.setUserId(userId);
		memberShip.setJoinDate(date);
		memberShip.setIP(friend.getSourceIp());
		memberShip.setBrowserIdx(friend.getBrowserIdx());
		memberShip.setAgentIdx(friend.getAgentIdx());
		memberShip.setFrequentChange(friend.isFrequentChange());
		memberShip.setHaveSmartPhone(friend.isHaveSmartPhone());
        memberShip.setLargePoster(friend.isLargePoster());
        memberShip.userCreationDate = friend.toCreationDate;
		return memberShip;
	}
	
	public GroupMemberShip createGroupMember(Random random, long userId, long groupCreatedDate, ReducedUserProfile user){

        long date = dateGenerator.randomDate(random, Math.max(groupCreatedDate, user.getCreationDate()+deltaTime));
        if( date > dateGenerator.getEndDateTime() ) return null;
        GroupMemberShip memberShip = new GroupMemberShip();
        memberShip.setUserId(userId);
        memberShip.setJoinDate(date);
        memberShip.setIP(user.getIpAddress());
        memberShip.setBrowserIdx(user.getBrowserIdx());
        memberShip.setAgentIdx(user.getAgentId());
        memberShip.setFrequentChange(user.isFrequentChange());
        memberShip.setHaveSmartPhone(user.isHaveSmartPhone());
        memberShip.setLargePoster(user.isLargePoster());
        memberShip.userCreationDate = user.getCreationDate();
        return memberShip;
    }
}
