package com.exa.android.reflekt.loopit.presentation.main.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.EditProfileViewModel
import com.exa.android.reflekt.loopit.presentation.main.Home.component.TrackableImage
import com.exa.android.reflekt.loopit.util.Response



@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillsCard(isCurUser : Boolean, skills: List<String>, editProfileViewModel: EditProfileViewModel = hiltViewModel()) {
    val skillInput by editProfileViewModel.skillInput
    val isEditing by editProfileViewModel.isEditing
    val updatedSkills by editProfileViewModel.updatedSkills
    val stagedSkills by editProfileViewModel.stagedSkills

    val skillState = editProfileViewModel.responseState

    LaunchedEffect(skills) {
        editProfileViewModel.initialiseSkill(skills.toMutableList())
    }

    LaunchedEffect(skillState) {
        if (skillState is Response.Success) {
            editProfileViewModel.onSuccess()
        }
    }

    val suggestions = remember(skillInput, updatedSkills, stagedSkills) {
        if (skillInput.length > 2) {
            getSkillSuggestions(skillInput)
                .filterNot { it in updatedSkills || it in stagedSkills }
        } else emptyList()
    }

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiary),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Skills", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiary)
            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                updatedSkills.forEach { skill ->
                    EditableChip(skill = skill, isEditing) {
                        editProfileViewModel.removeUpdatedSkill(skill)
                    }
                }


                if (!isEditing && isCurUser) {
                    AddSkillButton { editProfileViewModel.setEditing(true) }
                }
            }

            if (updatedSkills.isNotEmpty() && stagedSkills.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                Divider(
                    modifier = Modifier
                        .height(1.dp)
                        .width(24.dp)
                        .background(Color.LightGray)
                )
            }

            if (stagedSkills.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("New skills to add:", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    stagedSkills.forEach { skill ->
                        StagedSkillChip(skill) {
                            editProfileViewModel.removeStagedSkill(skill)
                        }
                    }
                }
            }

            if (isEditing) {
                Spacer(Modifier.height(12.dp))
                Column {
                    OutlinedTextField(
                        value = skillInput,
                        onValueChange = { editProfileViewModel.onSkillInputChanged(it) },
                        label = { Text("Add new skills") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                editProfileViewModel.addSkillToStaging()
                            }
                        ),
                        trailingIcon = {
                            if (skillInput.isNotBlank()) {
                                IconButton(
                                    onClick = { editProfileViewModel.addSkillToStaging() }
                                ) {
                                    Icon(Icons.Default.Add, "Add skill")
                                }
                            }
                        }
                    )

                    if (suggestions.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Suggestions:", style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(suggestions) { suggestion ->
                                SuggestionChip(suggestion) {
                                    editProfileViewModel.addSkillToStaging(suggestion)
                                }
                            }
                        }
                    } else if (skillInput.length > 2 && !editProfileViewModel.updatedSkills.value.contains(skillInput)) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Add \"$skillInput\"",
                            fontStyle = FontStyle.Italic,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .clickable { editProfileViewModel.addSkillToStaging() }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { editProfileViewModel.cancelEditing(skills.toMutableList()) },
                            enabled = skillState !is Response.Loading
                        ) {
                            Text("Cancel")
                        }

                        Spacer(Modifier.width(8.dp))

                        when (skillState) {
                            is Response.Loading -> {
                                Button(onClick = {}, enabled = false) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Saving...")
                                }
                            }

                            is Response.Error -> {
                                Button(
                                    onClick = { editProfileViewModel.saveSkills() },
                                    enabled = true
                                ) {
                                    Text("Retry")
                                }
                            }

                            is Response.Success, null -> {
                                Button(
                                    onClick = { editProfileViewModel.saveSkills() }
                                    //enabled = stagedSkills.isNotEmpty()
                                ) {
                                    Text("Save")
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}


@Composable
private fun EditableChip(skill: String, isEditing: Boolean, onRemove: () -> Unit) {
    var formattedSkill = skill.lowercase().split(" ").first()
    if (formattedSkill == "c++") formattedSkill = "cpp"

    // Set of all supported skillicons
    val supportedSkills = setOf(
        "ableton","activitypub","actix","adonis","ae","aiscript","alpinejs","anaconda","androidstudio",
        "angular","ansible","apollo","apple","appwrite","arch","arduino","astro","atom","au","autocad",
        "aws","azul","azure","babel","bash","bevy","bitbucket","blender","bootstrap","bsd","bun","c","cs",
        "cpp","crystal","cassandra","clion","clojure","cloudflare","cmake","codepen","coffeescript","css",
        "cypress","d3","dart","debian","deno","devto","discord","bots","discordjs","django","docker",
        "dotnet","dynamodb","eclipse","elasticsearch","electron","elixir","elysia","emacs","ember",
        "emotion","express","fastapi","fediverse","figma","firebase","flask","flutter","forth","fortran",
        "gamemakerstudio","gatsby","gcp","git","github","githubactions","gitlab","gmail","gherkin","go",
        "gradle","godot","grafana","graphql","gtk","gulp","haskell","haxe","haxeflixel","heroku",
        "hibernate","html","htmx","idea","ai","instagram","ipfs","java","js","jenkins","jest","jquery",
        "kafka","kali","kotlin","ktor","kubernetes","laravel","latex","less","linkedin","linux","lit",
        "lua","md","mastodon","materialui","matlab","maven","mint","misskey","mongodb","mysql","neovim",
        "nestjs","netlify","nextjs","nginx","nim","nix","nodejs","notion","npm","nuxtjs","obsidian",
        "ocaml","octave","opencv","openshift","openstack","p5js","perl","ps","php","phpstorm","pinia",
        "pkl","plan9","planetscale","pnpm","postgres","postman","powershell","pr","prisma","processing",
        "prometheus","pug","pycharm","py","pytorch","qt","r","rabbitmq","rails","raspberrypi","react",
        "reactivex","redhat","redis","redux","regex","remix","replit","rider","robloxstudio","rocket",
        "rollupjs","ros","ruby","rust","sass","spring","sqlite","stackoverflow","styledcomponents",
        "sublime","supabase","scala","sklearn","selenium","sentry","sequelize","sketchup","solidity",
        "solidjs","svelte","svg","swift","symfony","tailwind","tauri","tensorflow","terraform",
        "threejs","twitter","ts","ubuntu","unity","unreal","v","vala","vercel","vim","visualstudio",
        "vite","vitest","vscode","vscodium","vue","vuetify","wasm","webflow","webpack","webstorm",
        "windicss","windows","wordpress","workers","xd","yarn","yew","zig"
    )

    val finalSkill = if (supportedSkills.contains(formattedSkill)) formattedSkill else "htmx"
    val iconUrl = "https://skillicons.dev/icons?i=$finalSkill"

    Box(
        modifier = Modifier
            .width(64.dp)
            .padding(bottom = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box {
                if(iconUrl.isNullOrBlank()){
                   Image(
                       painter = painterResource(R.drawable.htmx_ic),
                       contentDescription = "failedIcon",
                       modifier = Modifier
                           .size(40.dp)
                           .clip(RoundedCornerShape(8.dp)),
                       contentScale = ContentScale.Crop
                   )
                }else{
                TrackableImage(
                    imageUrl = iconUrl,
                    contentDescription = "$skill Icon",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                )}

                if (isEditing) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .offset(x = 12.dp, y = (-4).dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { onRemove() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove skill",
                            modifier = Modifier.size(10.dp),
                            tint = Color.Black
                        )
                    }
                }
            }

            Text(
                text = skill,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}

@Composable
private fun AddSkillButton(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(64.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add skill",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Text(
            text = "Add New",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onTertiary
        )
    }
}

@Composable
private fun SuggestionChip(text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

private fun getSkillSuggestions(query: String): List<String> {
    val allSkills = getAllSkill()
    return allSkills.filter { it.contains(query, ignoreCase = true) }
}



@Composable
private fun StagedSkillChip(skill: String, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.primaryContainer,
                RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = skill,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}


private fun getAllSkill(): MutableList<String> {
    return mutableListOf(
        "Java", "Kotlin", "Python", "C++", "JavaScript", "TypeScript", "HTML", "CSS", "SQL",
        "NoSQL", "React", "Angular", "Vue.js", "Node.js", "Spring Boot", "Django", "Flask",
        "Ruby on Rails", "ASP.NET", "Laravel", "Swift", "Objective-C", "Go", "Rust", "Scala",
        "Perl", "PHP", "Firebase", "MongoDB", "PostgreSQL", "MySQL", "Oracle", "Redis",
        "Docker", "Kubernetes", "Jenkins", "Git", "GitHub", "Bitbucket", "CI/CD", "Agile",
        "Scrum", "Kanban", "Trello", "JIRA", "Confluence", "AWS", "Azure", "Google Cloud",
        "Heroku", "DigitalOcean", "Linux", "Unix", "Windows Server", "Cybersecurity",
        "Penetration Testing", "Ethical Hacking", "Network Security", "Cloud Security",
        "DevOps", "SRE", "Microservices", "RESTful APIs", "GraphQL", "SOAP", "JSON", "XML",
        "Machine Learning", "Deep Learning", "NLP", "Computer Vision", "TensorFlow", "PyTorch",
        "Keras", "Scikit-learn", "Pandas", "NumPy", "Matplotlib", "Seaborn", "Data Analysis",
        "Data Visualization", "Big Data", "Hadoop", "Spark", "Hive", "Pig", "Kafka", "Scala",
        "Tableau", "Power BI", "QlikView", "Excel", "VBA", "MATLAB", "Simulink", "AutoCAD",
        "SolidWorks", "ANSYS", "CATIA", "Revit", "Blender", "Unity", "Unreal Engine",
        "Game Development", "AR/VR Development", "Blockchain", "Smart Contracts", "Solidity",
        "Ethereum", "Bitcoin", "Cryptography", "IoT", "Embedded Systems", "Robotics",
        "Control Systems", "PLC Programming", "SCADA", "LabVIEW", "Signal Processing",
        "Image Processing", "Natural Language Processing", "Reinforcement Learning",
        "Quantum Computing", "Bioinformatics", "Genomics", "Proteomics", "Cheminformatics",
        "Pharmacovigilance", "Clinical Research", "Regulatory Affairs", "Medical Coding",
        "Healthcare IT", "Telemedicine", "EHR Systems", "LIMS", "SAS Programming",
        "Statistical Analysis", "Econometrics", "Financial Modeling", "Risk Management",
        "Actuarial Science", "Investment Banking", "Equity Research", "Portfolio Management",
        "Accounting", "Auditing", "Taxation", "SAP", "Oracle ERP", "Microsoft Dynamics",
        "Salesforce", "Zoho CRM", "HubSpot", "Digital Marketing", "SEO", "SEM", "Content Marketing",
        "Email Marketing", "Social Media Marketing", "Affiliate Marketing", "Google Analytics",
        "Google Ads", "Facebook Ads", "LinkedIn Ads", "Marketing Automation", "CRM Tools",
        "Adobe Photoshop", "Adobe Illustrator", "Adobe InDesign", "CorelDRAW", "Figma",
        "Sketch", "Adobe XD", "UI/UX Design", "Wireframing", "Prototyping", "User Research",
        "Usability Testing", "Human-Computer Interaction", "Information Architecture",
        "Typography", "Color Theory", "Branding", "Motion Graphics", "Video Editing",
        "After Effects", "Premiere Pro", "Final Cut Pro", "3D Modeling", "3D Animation",
        "Maya", "3ds Max", "Cinema 4D", "ZBrush", "Substance Painter", "Game Design",
        "Level Design", "Game Mechanics", "Game Balancing", "Sound Design", "Music Production",
        "Audio Engineering", "Mixing", "Mastering", "DAWs", "Ableton Live", "FL Studio",
        "Logic Pro", "Pro Tools", "Cubase", "Reaper", "Music Theory", "Composition",
        "Orchestration", "Conducting", "Arranging", "Songwriting", "Lyric Writing",
        "Music Notation", "Sibelius", "Finale", "MuseScore", "Guitar", "Piano", "Violin",
        "Drums", "Bass Guitar", "Singing", "Vocal Coaching", "Choral Singing", "Opera Singing",
        "Jazz Singing", "Pop Singing", "Rock Singing", "Rap", "Beatboxing", "DJing",
        "Turntablism", "Music Mixing", "Live Sound", "Stage Management", "Lighting Design",
        "Set Design", "Costume Design", "Makeup Artistry", "Hair Styling", "Fashion Design",
        "Textile Design", "Pattern Making", "Garment Construction", "Fashion Illustration",
        "Fashion Styling", "Visual Merchandising", "Retail Management", "E-commerce",
        "Dropshipping", "Product Photography", "Photo Editing", "Videography", "Cinematography",
        "Screenwriting", "Storyboarding", "Film Editing", "Color Grading", "Directing",
        "Producing", "Acting", "Voice Acting", "Theater Arts", "Dance", "Choreography",
        "Ballet", "Contemporary Dance", "Hip Hop Dance", "Jazz Dance", "Tap Dance",
        "Ballroom Dance", "Salsa", "Tango", "Zumba", "Yoga", "Pilates", "Personal Training",
        "Nutrition", "Diet Planning", "Wellness Coaching", "Life Coaching", "Public Speaking",
        "Motivational Speaking", "Event Planning", "Event Management", "Hospitality Management",
        "Hotel Management", "Travel Planning", "Tourism Management", "Culinary Arts",
        "Baking", "Pastry Arts", "Bartending", "Wine Tasting", "Sommelier", "Food Styling",
        "Food Photography", "Recipe Development", "Nutrition Science", "Food Safety",
        "Sanitation", "Restaurant Management", "Customer Service", "Call Center Operations",
        "Technical Support", "Help Desk Support", "IT Support", "Network Administration",
        "System Administration", "Database Administration", "Cloud Administration",
        "ITIL", "Project Management", "PMP", "PRINCE2", "Agile Coaching", "Scrum Master",
        "Business Analysis", "Business Intelligence", "Data Warehousing", "ETL", "Data Mining",
        "Predictive Analytics", "Prescriptive Analytics", "Descriptive Analytics",
        "Operations Research", "Supply Chain Management", "Logistics", "Procurement",
        "Inventory Management", "Warehouse Management", "Transportation Management",
        "Fleet Management", "Import/Export", "Customs Compliance", "Trade Compliance",
        "International Business", "Cross-Cultural Communication", "Language"
    )
}

