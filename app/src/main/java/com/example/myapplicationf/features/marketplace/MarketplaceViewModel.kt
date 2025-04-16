package com.example.myapplicationf.features.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplicationf.auth.AuthHelper
import com.example.myapplicationf.features.marketplace.models.BuyerDetail
import com.example.myapplicationf.features.marketplace.models.Category
import com.example.myapplicationf.features.marketplace.models.ListedCrop
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Date

data class CropItem(
    val name: String,
    val quantity: Int,
    val rate: Int,
    val location: String,
    val category: Category
)

// Data class to represent a taluka and its villages
data class Taluka(
    val name: String,
    val villages: List<String>
)

class MarketplaceViewModel : ViewModel() {
    private val db: FirebaseFirestore = Firebase.firestore
    private val cropsCollection = db.collection("crops")

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _selectedProduct = MutableStateFlow<ListedCrop?>(null)
    val selectedProduct: StateFlow<ListedCrop?> = _selectedProduct.asStateFlow()

    private val _crops = MutableStateFlow<List<CropItem>>(emptyList())
    val crops: StateFlow<List<CropItem>> = _crops.asStateFlow()

    private val _foodsByCategory = MutableStateFlow<Map<Category, List<String>>>(emptyMap())
    val foodsByCategory: StateFlow<Map<Category, List<String>>> = _foodsByCategory.asStateFlow()

    private val _listedCrops = MutableStateFlow<List<ListedCrop>>(emptyList())
    val listedCrops: StateFlow<List<ListedCrop>> = _listedCrops.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _customRate = MutableStateFlow("")
    val customRate: StateFlow<String> = _customRate.asStateFlow()

    // Taluka and village related state
    private val _talukas = MutableStateFlow<List<Taluka>>(emptyList())
    val talukas: StateFlow<List<Taluka>> = _talukas.asStateFlow()

    private val _selectedTaluka = MutableStateFlow<String?>(null)
    val selectedTaluka: StateFlow<String?> = _selectedTaluka.asStateFlow()

    private val _villages = MutableStateFlow<List<String>>(emptyList())
    val villages: StateFlow<List<String>> = _villages.asStateFlow()

    private val _filteredVillages = MutableStateFlow<List<String>>(emptyList())
    val filteredVillages: StateFlow<List<String>> = _filteredVillages.asStateFlow()

    // Selected crop for detail view
    private val _selectedCrop = MutableStateFlow<ListedCrop?>(null)
    val selectedCrop: StateFlow<ListedCrop?> = _selectedCrop.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        initializeFoodsByCategory()
        initializeSataraTalukasAndVillages()
        loadData()
    }

    private fun initializeSataraTalukasAndVillages() {
        // Initialize with talukas and their villages from Satara district
        val sataraTalukas = listOf(
            Taluka("Jaoli", listOf(
                "Adoshi", "Agalavewadi", "Akhade", "Akhegani", "Alewadi", "Ambeghar T Medha", "Ambeghar T. Kudal", "Andhari", "Anewadi", "Apti", "Arde", "Asani", "Bahule", "Bamnoli T. Kudal", "Belawade", "Beloshi", "Bhaleghar", "Bhamghar", "Bhanang", "Bhivadi", "Bhogavali T. Medha", "Bhogavali T. Kudal", "Bhuteghar", "Bibhvi", "Bondarwadi", "Chorambe", "Dabhe Turuk", "Dangreghar", "Dapawadi", "Dare Bk. ", "Dare Kh. ", "Deur", "Dhanakwadi", "Dhondewadi", "Divdev", "Divdevwadi", "Duduskarwadi", "Dund", "Furus", "Galdev", "Ganje", "Gavdi", "Ghoteghar", "Gondemal", "Hateghar", "Humgaon", "Indavli", "Jambruk", "Jarewadi", "Jawalwadi", "Jungti", "Kaloshi", "Karandi T. Medha", "Karandoshi", "Karanje", "Karedi T. Kudal", "Kargaon", "Karhar", "Kas", "Kasbe Bamnoli", "Katavali", "Kavadi", "Kedambe", "Kelghar T Medha", "Kelghar T. Solshi", "Kenjal", "Kesakarwadi", "Kharshi Baramure", "Kharshi T. Kudal", "Khirkhandi", "Kolewadi", "Kolghar", "Kudal", "Kumbhargani", "Kuraloshi", "Kusapur", "Kusawade", "Kusumbi", "Madoshi", "Mahigaon", "Mahu", "Majare Shevandi", "Malchoundi", "Maldev", "Maleshwar", "Mamurdi", "Manti", "Maradmure", "Marli", "Mauje Shevandi", "Met Indawli", "Met Shindi", "Mhamulkarwadi", "Mhasve", "Mhate Bk", "Mhate Kh. ", "Mhavshi", "Mohat", "Morawale", "Morghar", "Mukavale", "Munawale", "Nandgane", "Narfdev", "Nipani", "Nizare", "Okhavadi", "Ozare", "Pali T. Tamb", "Panas", "Pawarwadi", "Phalani", "Pimpali", "Pimpri T. Medha", "Prabhuchiwadi", "Punavadi", "Ramwadi", "Rangeghar", "Ranjni", "Ravandi", "Raygaon", "Rendimura", "Rengadiwadi", "Ritkawali", "Ruighar", "Sahyadrinagar", "Sanapane", "Sangvi T. Kudal", "Sangvi T. Medha", "Sarjapur", "Sartale", "Savali", "Sawari", "Sawrat", "Sayali", "Saygaon", "Sayghar", "Shete", "Shindewadi", "Somardi", "Songaon", "Takawli", "Taloshi", "Tambi", "Tambi T. Medha", "Tetli", "Umbarewadi", "Vadgare", "Vagheshwar", "Vahagaon", "Vahite", "Valanjwadi", "Valuth", "Varoshi", "Vasota", "Vatambe", "Vele", "Vivar", "Waghali", "Waki", "Yekiv"

            )),
            Taluka("Karad", listOf(
                "Abachiwadi", "Akaichiwadi", "Ambavade", "Andharwadi", "Ane", "Antvadi", "Arewadi", "Atake", "Babarmachi", "Bamanwadi", "Bandakerwadi", "Banugadewadi", "Belavade Bk", "Beldari", "Belvade Haveli", "Belwadi", "Bhagatwadi", "Bharewadi", "Bhavanwadi", "Bholewadi", "Bhosalewadi", "Bhurbhushi", "Bhuyachiwadi", "Botrewadi", "British - Shirwade", "Chachegaon", "Chapnemala", "Charegaon", "Chaugulemala", "Chikhali", "Chinchani", "Chorajwadi", "Chore", "Chormarwadi", "Daphalwadi", "Delewadi", "Dhanakwadi", "Dhavarwadi", "Dhondewadi", "Dushere", "Gaikwadwadi", "Gamewadi", "Ganeshwadi", "Gharalwadi", "Gharewadi", "Ghogaon", "Gholapwadi", "Ghonasi", "Godwadi", "Goleshwar", "Gondi", "Gopalnagar", "Gosavewadi", "Gote", "Gotewadi", "Govare", "Hanbarwadi", "Hanumantwadi", "Hanumanwadi", "Harpalwadi", "Havelwadi", "Helgaon", "Hingnole", "Indoli", "Jadhavmala", "Jakhinwadi", "Jinti", "Julewadi", "Junjarwadi", "Kacharewadi", "Kalantrewadi", "Kale", "Kaletake", "Kalgaon", "Kalvade", "Kamathi", "Kambirwadi", "Kapil", "Karavadi", "Karve", "Kasarshirambe", "Kavathe", "Kese", "Khalkarwadi", "Kharade", "Khodjaiwadi", "Khodshi", "Khubi", "Kirpe", "Kival", "Kodoli", "Kole", "Kolewadi", "Konegaon", "Koparde Haveli", "Koregaon", "Koriwale", "Korti", "Koyanavasahat", "Kusur", "Latakewadi", "Loharwadi", "Maharugadewadi", "Malkhed", "Malwadi", "Mangwadi", "Manu", "Marali", "Maskarwadi", "Masur", "Matekerwadi", "Mervawadi", "Mhasoli", "Mhopre", "Munawale", "Mundhe", "Nadashi", "Nandgaon", "Nandlapur", "Narayanwadi", "Navin Gaonthun Gharewadi", "Navin Kavathe", "Navin Nandgaon", "Navinmalkheda", "Nigadi", "Ond", "Ondosha", "Pachund", "Pachupatewadi", "Pachwadwasahat", "Padali", "Padalikese", "Pal", "Parale", "Parle", "Pashchimsupane", "Patharwadi", "Patilmala", "Pawarwadi", "Pawarwadi", "Pimpari", "Potale", "Rajmachi", "Rethare Bk. ", "Rethare Kh. ", "Risvad", "Sabalwadi", "Sajur", "Sakurdi", "Salshirambe", "Sanjaynagar", "Sanjaynagar", "Savade", "Sayapur", "Shahapur", "Shakerwadi", "Shamgaon", "Shelakewadi", "Shelekewadi", "Shendawadi", "Shenoli", "Shenolistalion", "Shere", "Shevalwadi", "Shevalwadi", "Shevalwadi", "Shewalewadi", "Shindewadi", "Shinganwadi", "Shiravade", "Shirgaon", "Shitalwadi", "Shiwade", "Supane", "Surli", "Talbid", "Talgaon", "Tambave", "Tarukh", "Tasavade", "Tembhu", "Thoerathmal", "Thoratmala", "Tulsan", "Umbraj", "Undale", "Uttar Tambave", "Vadgaon Umbraj", "Vadoli Bhikeshwar", "Vadoli Nileshwar", "Vagheri", "Vahagaon", "Vanarwadi", "Vanavasmachi", "Vanvadi (Sadashivgad)", "Vanvasmachi", "Varade", "Varunji", "Vasantgad", "Vastisakurdi", "Vathar", "Vijaynagar", "Ving", "Virvade", "Vithobawadi", "Wadgaon Haveli", "Wagheshwar", "Wanyachiwadi", "Wazewadi", "Yadavawadi", "Yadavwadi", "Yelgaon", "Yenake", "Yenape", "Yeravale", "Yeshwantnagar", "Yevati"

            )),
            Taluka("Khandala", listOf(
                "Ahire", "Ajnuj", "Ambarwadi", "Andori", "Asawali", "Atit", "Balu Patlachiwadi", "Bavda", "Bavkalwadi", "Bhadavade", "Bhade", "Bhatghar", "Bholi", "Bori", "Dhangarwadi", "Dhawadwadi", "Ghadgewadi", "Ghatdare", "Guthalwadi", "Harali", "Hartali", "Javale", "Kanhavadi", "Kanheri", "Karadwadi", "Karnavadi", "Kavathe", "Kesurdi", "Khandala", "Khed Bk. ", "Koparde", "Limachiwadi", "Lohom", "Loni", "Mane Colony", "Mariachiwadi", "Mhavashi", "Mirje", "Moh Tarf Shirwal", "Morve", "Naigaon", "Nimbodi", "Padali", "Padegaon", "Palashi", "Pargaon", "Pimpare Bk. ", "Pisalwadi", "Rajewadi", "Rui", "Sangvi", "Shedgewadi", "Shekhmirwadi", "Shindewadi", "Shirwal", "Shivajinagar", "Sukhed", "Tondal", "Vadgaon", "Wadwadi", "Waghoshi", "Wanyachiwadi", "Wathar Bk. ", "Wing", "Yelewadi", "Zagalwadi"

            )),
            Taluka("Khatav", listOf(
                "Amalewadi", "Ambavade", "Ambheri", "Anpatwadi", "Anphale", "Aundh", "Banpuri", "Bhandewadi", "Bhosare", "Bhurakvadi", "Bhushangad", "Bitalewadi", "Bombale", "Budh", "Chinchani", "Chitali", "Chorade", "Dalmodi", "Dambhewadi", "Darajai", "Daruj", "Datewadi", "Dhakarwadi", "Dharpudi", "Dhokalwadi", "Dhondewadi", "Diskal", "Enkul", "Fadtarwadi", "Fadtarwadi Bk", "Gadewadi", "Ganeshwadi", "Ganeshwadi", "Garalewadi", "Garavadi", "Garudi", "Girijashankarwadi", "Gopuj", "Goregaon (N) (Vangi)", "Goregaon (Vangi)", "Gosavyachiwadi", "Gundewadi", "Gursale", "Hingane", "Hivarwadi", "Holichagaon", "Husenpur", "Jaigaon", "Jakhangaon", "Jamb", "Kalambi", "Kaledhon", "Kalewadi", "Kamathi T. Parali (N. V. )", "Kanharwadi", "Kankatre", "Kansewadi", "Karandewadi", "Katalgewadi", "Katarkhatav", "Katewadi", "Katgun", "Khabalwadi", "Kharashinge", "Khatav", "Khatgun", "Khatval", "Kokarale", "Kumathe", "Kurle", "Kuroli", "Ladegaon", "Lalgun", "Landewadi", "Laxminagar", "Loni", "Mandave", "Manetupewadi", "Manjarwadi", "Maradwak", "Mayani", "Mhasurne", "Mol", "Morale", "Mulikwadi", "Musandewadi", "Nagnathawadi", "Naikachiwadi", "Nandoshi", "Nathaval", "Navalewadi", "Ner", "Nidhal", "Nimsod", "Pachwad", "Padal", "Palasgaon", "Palashi", "Pandharwadi", "Pangarkhel", "Pargaon", "Pawarwadi", "Pedgaon", "Pimpari", "Pophalkarwadi (N. V. )", "Pusegaon", "Pusesawali", "Rahatani", "Rajapur", "Rameshwar", "Ramoshiwadi", "Ranshingwadi", "Revalkarwadi", "Satewadi", "Shenawadi", "Shendgewadi", "Shindewadi", "Shirsawadi", "Sundarpur", "Suryachiwadi", "Tadavale", "Taraswadi", "Thoravewadi", "Trimali", "Umbarde", "Umbarmale", "Unchithane", "Vadgaon (J. S. )", "Vadi", "Vadkhal", "Vaduj", "Vanzoli", "Vardhangad", "Varud", "Vetane", "Vikhale", "Visapur", "Wakalwadi", "Wakeshwar", "Yaralwadi", "Yeliv", "Yelmarwadi"

            )),
            Taluka("Koregaon", listOf(
                "Ambawade S. Koregaon", "Ambawade S. Wagholi", "Ambheri", "Anbhulwadi", "Anpatwadi", "Apshinge", "Arabwadi", "Arvi", "Asangaon", "Asgaon", "Bagewadi", "Banawadi", "Belewadi", "Bhadale", "Bhakarwadi", "Bhaktawadi", "Bhandarmachi", "Bhatamwadi", "Bhavenagar", "Bhimnagar", "Bhivadi", "Bhose", "Bichukale", "Bobadewadi", "Bodhewadi", "Bodhewadi", "Borgaon", "Borjaiwadi", "Chadvadi", "Chanchali", "Chaudharwadi", "Chawaneshwar", "Chilewadi", "Chimangaon", "Dahigaon", "Dare T. Tamb", "Deur", "Dhamner", "Dhumalwadi", "Dudhanwadi", "Dudhi", "Durgalwadi", "Ekambe", "Eksal", "Fadtarwadi", "Ghigewadi", "Godsewadi", "Gogavalewadi", "Golewadi", "Gujarwadi", "Gujarwadi", "Hasewadi", "Hivare", "Holewadi", "Jadhavwadi", "Jagtapwadi", "Jaigaon", "Jalgaon", "Jamb Bk. ", "Jamb Kh. ", "Jarewadi", "Kaloshi", "Kanherkhed", "Karanjkhop", "Katewadi", "Kathapur", "Kawadewadi", "Khadkhadwadi", "Khamakarwadi", "Khed", "Khirkhindi", "Kinhai", "Kiroli", "Kolavadi", "Kombadwadi", "Kumathe", "Lhasurne", "Madanapurwadi", "Mangalapur", "Mohitewadi", "Moreband", "Mugaon", "Nagewadi", "Nagzari", "Nalavadewadi", "Nalavadewadi", "Nandwal", "Naygaon", "Nhavi Bk", "Nhavi Kh. ", "Nigadi", "Palashi", "Paratwadi", "Pawarwadi", "Pimpode Bk. ", "Pimpode Kh. ", "Pimpri", "Ramoshiwadi", "Randullabad", "Rautwadi", "Revdi", "Rikibadarwadi", "Rui", "Saigaon", "Saigaon", "Sangavi", "Sap", "Sarkalwadi", "Sasurve", "Satara Road", "Sathewadi", "Shahapur", "Shelti", "Shendurjane", "Shirambe", "Shirdhon", "Siddharthnagar", "Solashi", "Sonake", "Sultanwadi", "Surli", "Tadawale S. Wagholi", "Tadawale S. Koregaon", "Takale", "Talaye", "Tambi", "Tandulwadi", "Targaon.", "Triputi", "Vadachiwadi", "Velang", "Velang", "Velu", "Vikhale", "Waghajaiwada", "Wagholi", "Wathar Kiroli", "Wathar Station"

            )),
            Taluka("Mahabaleshwar", listOf(
                "Achali", "Adhal", "Ahire", "Akalpe", "Ambral", "Amshi", "Araw", "Avakali", "Awalan", "Bhekavali", "Bhilar", "Bhose", "Birmāni", "Birwadi", "Chakdev", "Chaturbet", "Chikhali", "Dabhe Dabhekar", "Dabhe Mohan", "Dandeghar", "Danvali", "Dare", "Dare Tamb", "Devali", "Devasare", "Dhangarwadi", "Dhardev", "Dhawali", "Dodani", "Dudhgaon", "Dudhoshi", "Gadhavali", "Gavadhoshi", "Ghavari", "Ghonaspur", "Godavali", "Gogave", "Goroshi", "Gureghar", "Harchandi", "Haroshi", "Hatlot", "Jaoli", "Kalamgaon", "Kalamgaon Kalamkar", "Kandat", "Kasrud", "Kaswand", "Khambil Chorge", "Khambil Pokale", "Kharoshi", "Khengar", "Kotroshi", "Kshetra Mahabaleshwar", "Kumbharoshi", "Kumthe", "Kuroshi", "Lakhwad", "Lamaj", "Machutar", "Majarewadi", "Malusar", "Manghar", "Met Taliye", "Metgutad", "Mhalunge", "Moleshwar", "Morni", "Nakinda", "Navali", "Nivali", "Pali T. Ategaon", "Pangari", "Parpar", "Parsond", "Parut", "Parwat T. Wagawale", "Petpar", "Pimpri T. Tamb", "Rajpuri", "Rameghar", "Ran Adva Gaund", "Renoshi", "Rule", "Saloshi", "Shindi", "Shindola", "Shiravali", "Shirnar", "Sonat", "Soundari", "Taighat", "Taldev", "Tapola", "Tekavali", "Uchat", "Umbari", "Valawan", "Vanavli T. Ategaon", "Vanavli T. Solasi", "Varsoli Dev", "Varsoli Koli", "Velapur", "Vengale", "Vivar", "Walne", "Yerandal", "Yerne Bk", "Yerne Kh", "Zadani", "Zanzwad"

            )),
            Taluka("Man", listOf(
                "Agaswadi", "Andhali", "Anubhulewadi", "Bangarwadi", "Bhalavadi", "Bhandavali", "Bhatki", "Bidal", "Bijavadi", "Bodake", "Bothe", "Chillarwadi", "Dahivadi", "Danavalewadi", "Dangirewadi", "Dangirewadi", "Devapur", "Dhakani", "Dhamani", "Dhuldev", "Didwaghwadi (Divad)", "Divad", "Divadi (Mahimangad)", "Dorgewadi (Naravane)", "Gadewadi", "Gangoti", "Garadachiwadi (Varugad)", "Gatewadi", "Gherewadi", "Gondavale Bk.", "Gondavale Kh.", "Hawaldarwadi (Paryanti)", "Hingani", "Injabav", "Jadhavwadi", "Jambhulani", "Jashi", "Kalaskarwadi (Kulakjai)", "Kalchondi", "Kalewadi (Naravane)", "Karkhel", "Kasarwadi (Andhali)", "Khadaki", "Khandyachiwadi(Varugad)", "Khokade", "Khutbav", "Kiraksal", "Kolewadi", "Kukudwad", "Kulakjai", "Kuranwadi", "Lodhavade", "Mahabaleshwar Wadi", "Mahimangad", "Malavadi", "Mankarnawadi", "Mardi", "Mogarale", "Mohi", "Naravane", "Pachvad", "Palashi", "Palsavade", "Panavan", "Pandharwadi (Mahimangad)", "Pangari", "Parkhandi", "Paryanti", "Pimpari", "Pingali Bk", "Pingali Kh.", "Pukalewadi", "Pulkoti", "Rajavadi", "Ranand", "Ranjani", "Sambhukhed", "Satrewadi (Malavadi)", "Shenwadi", "Shevari", "Shindi Bk.", "Shindi Kh", "Shinganapur", "Shiravali", "Shirtav", "Shripanvan", "Sokasan", "Swarupkhanwadi (Mahimangad)", "Takewadi (Andhali)", "Thadale", "Tondale", "Ugalyachiwadi (Varugad)", "Ukirde", "Vadgaon", "Valai", "Varkute Malavadi", "Varugad", "Virali", "Virobanagar", "Wadjal", "Waki", "Warkute Mhaswad", "Wawarhire", "Yelegaon"

            )),
            Taluka("Patan", listOf(
                "Aadev Kh.", "Abdarwadi", "Acharewadi", "Adadev", "Adul", "Ambale", "Ambavade Kh.", "Ambavane", "Ambeghar Tarf Marli", "Ambewadi", "Ambrag", "Ambrule", "Aral", "Asawalewadi", "Atoli", "Awarde", "Bacholi", "Bagalwadi", "Bahe", "Bahule", "Baje", "Bamanewadi", "Bamanwadi", "Bambavade", "Bandhvat", "Banpethwadi", "Banpuri", "Belavade Kh.", "Bhambe", "Bharewadi", "Bharsakhale", "Bhilarwadi", "Bhosgaon", "Bhudakewadi", "Bibi", "Bodakewadi", "Bondri", "Bopoli", "Boragewadi", "Borgewadi", "Borgewadi", "Borgewadi", "Chafal", "Chafer", "Chafoli", "Chalkewadi", "Chavanwadi", "Chavanwadi (Dhamani)", "Chawaliwadi", "Chikhalewadi", "Chiteghar", "Chopadi", "Chopdarwadi", "Chougulewadi", "Chougulewadi", "Dadholi", "Dakewadi (Kalgaon)", "Dakewadi (Wazoli)", "Dangistewadi", "Daphalwadi", "Davari", "Dervan", "Deshmukhwadi", "Devghar Tarf Patan", "Dhadamwadi", "Dhajgaon", "Dhamani", "Dhangarwadi", "Dhavade", "Dhayati", "Dhebewadi", "Dhokawale", "Dhoroshi", "Dhuilwadi", "Dicholi", "Digewadi", "Dikshi", "Divashi Bk.", "Divashi Kh", "Dongarobachiwadi", "Donglewadi", "Donichawada", "Dusale", "Dutalwadi", "Ekavadewadi", "Fartarwadi", "Gadhav Khop", "Galmewadi", "Gamewadi", "Garawade", "Gavhanwadi", "Gawalinagar", "Gawdewadi", "Gaymukhwadi", "Ghanav", "Ghanbi", "Ghatewadi", "Ghatmatha", "Gheradategad", "Ghot", "Ghotil", "Giraswadi", "Girewadi", "Gokul Tarf Helwak", "Gokul Tarf Patan", "Gorewadi", "Goshatwadi", "Gothane", "Govare", "Gudhe", "Gujarwadi", "Gunjali", "Guteghar", "Harugdewadi", "Helwak", "Humbarli", "Humbarne", "Humbarwadi", "Jadhavwadi", "Jadhavwadi", "Jaichiwadi", "Jalagewadi", "Jalu", "Jambhalwadi", "Jambhekarwadi", "Jamdadwadi and Chaugulewadi", "Jangal Wadi", "Janugdewadi", "Jarewadi", "Jinti", "Jugaiwadi", "Jungati", "Jyotibachiwadi", "Kadave Bk.", "Kadave Kh", "Kadhane", "Kadoli", "Kahir", "Kalambe", "Kalgaon", "Kalkewadi", "Kaloli", "Kamargaon", "Karale", "Karapewadi", "Karate", "Karpewadi", "Karvat", "Kasani", "Kasrund", "Katewadi", "Kathi", "Katvadi", "Kavadewadi", "Kavarwadi", "Keloli", "Kemase", "Ker", "Keral", "Khale", "Kharadwadi", "Khilarwadi", "Khivashi", "Khonoli", "Killemorgiri", "Kisrule", "Kocharewadi", "Kodal", "Kokisare", "Kolagewadi", "Kolane", "Kolekarwadi", "Kondhavale", "Konjavade", "Korivale", "Kotawadewadi", "Kumbhargaon", "Kusavade", "Kushi", "Kuthare", "Lendhori", "Letamewadi", "Loharwadi", "Lotalewadi", "Lugadewadi", "Mahind", "Majgaon", "Mala", "Maldan", "Malharpeth", "Maloshi", "Manainagar", "Mandrulkole", "Mandrulkole Kh.", "Mandure", "Maneri", "Manewadi", "Manyachiwadi", "Manyachiwadi", "Marali", "Marathwadi", "Marathwadi", "Marekarwadi", "Marloshi", "Marul Haveli", "Marul Tarf Patan", "Maskarwadi", "Maskarwadi", "Maskarwadi No. 1", "Mastewadi", "Mathanewadi", "Matrewadi", "Maulinagar", "Maundrul Haveli", "Mendh", "Mendheghar", "Mendhoshi", "Mharwand", "Mhavashi", "Mirgaon", "Modakwadi", "Morewadi (Kuthare)", "Morgiri", "Mulgaon", "Murud", "Muttalwadi", "Nade", "Nadoli", "Nahimbe", "Nanegaon Bk.", "Nanegaon Kh.", "Nanel", "Naralwadi", "Natoshi", "Nav", "Navadi", "Navasarwadi", "Nawaja", "Nechal", "Nerale", "Nigade", "Nisare", "Nivade", "Nivakane", "Nivi", "Nune", "Pabhalwadi", "Pachgani", "Pachupatewadi", "Padekarwadi", "Padharwadi Telewadi", "Padloshi", "Pagewadi", "Palashi", "Pandharwadi", "Paneri", "Paparde Bk", "Paparde Kh.", "Patharpunj", "Pathavade", "Pawarwadi", "Pawarwadi", "Petekarwadi", "Pethshivapur", "Pimpaloshi", "Punvali", "Rahude", "Ramishtewadi", "Rasati", "Risawad", "Ruvale", "Sabalewadi", "Sadawaghapur", "Saikade", "Sakhari", "Salave", "Saltewadi", "Sanbur", "Sangwad", "Satar", "Sawantwadi", "Sawarghar", "Shedgewadi", "Shendewadi (Kumbhargaon)", "Shibewadi", "Shidrukwadi", "Shindewadi", "Shinganwadi", "Shiral", "Shirshinge", "Shitapwadi", "Shivandeshwar", "Siddheshwar Nagar", "Sonaichiwadi", "Sonavade", "Subhashnagar", "Sulewadi", "Supugadewadi", "Surul", "Sutarwadi", "Taliye", "Tamine", "Tamkade", "Tamkane", "Tarale", "Taygadewadi", "Telewadi", "Thankal", "Thomase", "Tolewadi", "Tondoshi", "Torane", "Tripudi", "Tupewadi", "Udhavane", "Umarkanchan", "Urul", "Vadi Kotawade", "Vaichalwadi", "Vajegaon", "Vajegaon", "Vajroshi", "Van", "Vanzole", "Varekarwadi", "Varpewadi", "Vatole", "Vekhandwadi", "Vetalwadi", "Vihe", "Virewadi", "Vittalwadi", "Waghane", "Wagjaiwadi", "Wazoli", "Yelavewadi", "Yerad", "Yeradwadi", "Yerphale", "Zadoli", "Zakade"

            )),
            Taluka("Phaltan", listOf(
                "Adarki Bk.", "Adarki Kh.", "Alagudewadi", "Aljapur", "Andrud", "Aradgaon", "Asu", "Bagewadi", "Barad", "Bhadali Bk.", "Bhadali Kh.", "Bhawaninagar (N. V)", "Bhilkatti", "Bibi", "Bodkewadi", "Chambharwadi", "Chaudharwadi", "Chavhanwadi", "Dalvadi", "Dattanagar (N. V)", "Dhaval", "Dhavalewadi", "Dhavalewadi", "Dhuldev", "Dhumalwadi", "Dombalwadi", "Dudhebavi", "Fadatarwadi", "Farandwadi", "Ghadge Mala", "Ghadgewadi", "Girvi", "Gokhali", "Gunware", "Hanmantwadi", "Hingangaon", "Hol", "Jadhavwadi", "Jadhavwadi", "Jaoli", "Jinti", "Kalaj", "Kambleshwar", "Kapadgaon", "Kapashi", "Kashiwadi", "Khadaki", "Khamgaon", "Kharadewadi", "Khatkewasti", "Khunte", "Koparde", "Koregaon", "Korhale", "Kurvali Bk.", "Kurvali Kh.", "Kusur", "Malewadi", "Malvadi", "Mandavkhadak", "Manewadi", "Mathachiwadi", "Mirdhe", "Mirewadi", "Mirewadi", "Mirgaon", "Mulikwadi", "Munjwadi", "Murum", "Naik Bombawadi", "Nandal", "Nimbhore", "Nimblak", "Nirugudi", "Padegaon", "Pawarwadi", "Pimpalwadi", "Pimparad", "Pirachiwadi", "Rajale", "Rajuri", "Ravadi Bk.", "Ravadi Kh.", "Salpe", "Sangavi", "Sarade", "Saskal", "Sastewadi", "Saswad", "Sathe", "Sherechiwadi", "Sherechiwadi", "Shereshindewadi", "Shindemal", "Shindenagar", "Shindewadi", "Somanthali", "Songaon", "Sonwadi Bk.", "Sonwadi Kh.", "Survadi", "Tadavale", "Takalwade", "Tambave", "Taradgaon", "Tardaf", "Tathavada", "Tavadi", "Thakubaichiwadi", "Thakurki", "Tirakwadi", "Upalave", "Vadale", "Vadgaon", "Vadjal", "Vajegaon (N. V)", "Veloshi", "Vidani", "Vinchurni", "Vitthalwadi", "Waghoshi", "Wakhari", "Wathar (Nimbalkar)", "Zadakbaichiwadi", "Zirapwadi"

            )),
            Taluka("Satara", listOf(
                "Agudewadi", "Akale", "Alawadi", "Ambale", "Ambavade Bk", "Ambavade Kh.", "Ambewadi", "Anavale", "Angapur T Targaon", "Angapur Vandan", "Apashinge", "Arale", "Are T. Parali", "Argadwadi", "Arphal", "Asangaon", "Asgaon", "Ashte", "Atali", "Atit", "Banghar", "Basappachiwadi", "Bendwadi", "Bhairavgad (N. V.)", "Bhambavali", "Bharatgaon", "Bharatgaonwadi", "Bhatmarali", "Bhondavade", "Boposhi", "Borgaon", "Borkhal", "Borne", "Bramhanwadi", "Chahur", "Chalkewadi", "Chikhali", "Chinchaner S. Nimb", "Chinchaner Vandan", "Chinchani", "Chorgewadi", "Dabewadi", "Dahivad", "Dare Bk.", "Dare Kh.", "Dare T. Parali", "Degaon", "Deshmukh Nagar", "Dhanavadewadi", "Dhangarwadi", "Dhangarwadi", "Dhavali", "Dhawadshi", "Dhondewadi", "Dhondewadi", "Didhavale", "Dolegaon", "Fadatarwadi", "Fatyapur", "Gajawadi", "Ganeshwadi", "Gavadi", "Ghatawan", "Gogavalewadi", "Gojegaon", "Gove", "Hamdabaj", "Jadhavwadi", "Jaitapur", "Jakatwadi", "Jambhe", "Jawalwadi", "Jihe", "Jotibachiwadi", "Kalambe", "Kaloshi", "Kamathi T. Parali", "Kamathi T. Satara", "Kameri", "Kanher", "Karandi", "Karandwadi", "Karanje T. Parali", "Karanjoshi", "Kari", "Kasani", "Kasarthal", "Kashil", "Katavadi Bk", "Katavadi Kh", "Kelavali", "Khadegaon", "Khandobachi Wadi", "Khavali", "Khindwadi", "Khodad", "Khojewadi", "Kidgaon", "Kondhave", "Kondvali", "Koparde", "Kshetra Mahuli", "Kudeghar", "Kumathe", "Kurulbaji", "Kurultijai", "Kurun", "Kus Bk.", "Kus Kh.", "Kusavade", "Kushi", "Landewadi", "Lavanghar", "Limb", "Limbachiwadi", "Lumanekhol", "Mahagaon", "Majgaon", "Malgaon", "Malyachi Wadi", "Mandave", "Mardhe", "Matyapur", "Mhaparwadi", "Mhasave", "Mhaskarwadi", "Morewadi", "Mugdul Bhatachiwadi", "Mulikwadi", "Nagewadi", "Nagthane", "Nandgaon", "Navali", "Nele", "Nhalewadi", "Nigadi T. Satara", "Nigadi Vandan", "Nigudamal", "Ninam", "Nisarale", "Nitral", "Nune", "Padali", "Palsavade", "Pangare", "Panmalewadi", "Parali", "Parambe", "Parmale", "Pateghar", "Patkhal", "Petri", "Pilani", "Pilaniwadi", "Pimpalwadi", "Pirwadi", "Pogarwadi", "Punavadi", "Raighar", "Rajapuri", "Rakusalewadi", "Ramkrushna Nagar", "Renavale", "Revali", "Revande", "Rohot", "Sabalewadi", "Saidapur", "Salwan", "Sambarwadi", "Sandavali", "Sangam Mahuli", "Sarkhal", "Saspade", "Savali", "Sayali", "Sayali", "Shahapur", "Shelkewadi", "Shendre", "Sherewadi", "Shindewadi", "Shivthar", "Sonapur", "Sonavadi", "Sonegaon T Satara", "Songaon S. Nimb", "Takawali", "Tasgaon", "Thoseghar", "Titavewadi", "Tukaichiwadi", "Upali", "Vaduth", "Valase", "Vangal", "Vanvaswadi", "Varne", "Varye", "Vechale", "Vele", "Venegaon", "Venekhol", "Vijay Nagar", "Wadgaon", "Wadhe", "Wasole", "Wavadare", "Yavateshwar", "Zarewadi"

            )),
            Taluka("Wai", listOf(
                "Abhepuri", "Akoshi", "Ambedara", "Amrutwadi", "Anavadi", "Anpatwadi", "Asagaon", "Asale", "Asare", "Badewadi (N. V.)", "Balakavadi", "Baleghar", "Bavdhan", "Belamachi", "Bhirdachiwadi (N. V.)", "Bhivadi", "Bhivadi (R. H. V.)", "Bhogaon", "Bhuinj", "Bopardi", "Bopegaon", "Borgaon Bk.", "Borgaon Kh.", "Boriv", "Chandak", "Chandvadi (R. H. V.)", "Chikhali", "Chindhawali", "Chorachiwadi", "Dahyat", "Darewadi", "Dasvadi", "Degaon", "Dhavadi", "Dhavali", "Dhom", "Duichivadi", "Ekasar", "Gadhavewadi", "Gherakelanja", "Golegaon", "Golewadi", "Gove", "Govedigar", "Gulumb", "Gundewadi", "Jamb", "Jambhali", "Jambhulane", "Jor", "Kadegaon", "Kalambhe", "Kalangwadi", "Kanur", "Kawathe", "Kenjal", "Khadaki", "Khalachi Belmachi", "Khanapur", "Khavali", "Kholvadi", "Kikali", "Kironde", "Kisanvirnagar", "Kochalewadi", "Kondhavale", "Kondhavali Bk", "Kondhavali Kh", "Kusgaon", "Logadwadi", "Lohare", "Malatpur", "Maldevwadi (N. V.)", "Malusurewadi", "Mandhardeo", "Maparwadi / Vapanwadi", "Menavali", "Mohodekarwadi", "Mugaon", "Mungasewadi", "Nagewadi", "Nandgane", "Nhalewadi", "Nikamwadi", "Oholi", "Ozarde", "Pachputewadi (N. V.)", "Panas", "Panchwad", "Pande", "Pandewadi", "Pandharechiwadi", "Paratavadi", "Parkhandi", "Pasarni", "Pirachiwadi", "Purna Vyahali (Rhv)", "Rautwadi", "Renawale", "Satalewadi", "Shahabag (N. V.)", "Shelarwadi", "Shendurjane", "Shirgaon", "Sidhanathwadi (Rural)", "Sultanpur", "Surur", "Udatare", "Ulumb", "Vadoli", "Vahagaon", "Vaigaon", "Varkhadwadi", "Vasole", "Velang", "Vele", "Virmade", "Vithalwadi", "Vyahali", "Vyajawadi", "Wadachiwadi (N. V.)", "Wadkarwadi", "Wai (Rural)", "Washivali", "Yashwantnagar (N. V.)", "Yeruli"

            ))
        )

        _talukas.value = sataraTalukas
    }

    fun setSelectedTaluka(talukaName: String?) {
        _selectedTaluka.value = talukaName
        if (talukaName != null) {
            // Update villages list based on selected taluka
            val selectedTalukaData = _talukas.value.find { it.name == talukaName }
            _villages.value = selectedTalukaData?.villages ?: emptyList()
            _filteredVillages.value = _villages.value
        } else {
            _villages.value = emptyList()
            _filteredVillages.value = emptyList()
        }
    }

    fun searchVillage(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _filteredVillages.value = _villages.value
            } else {
                _filteredVillages.value = _villages.value.filter { village ->
                    village.startsWith(query, ignoreCase = true)
                }
            }
        }
    }

    private fun initializeFoodsByCategory() {
        _foodsByCategory.value = mapOf(
            Category.GRAINS to listOf("Wheat", "Rice", "Corn", "Millet", "Jowar", "Bajra"),
            Category.VEGETABLES to listOf("Tomatoes", "Potatoes"),
            Category.FRUITS to listOf("Bananas", "Grapes", "Strawberries"),
            Category.OILSEEDS to listOf("Soybean")
        )
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: Category?) {
        _selectedCategory.value = category
    }

    fun setSelectedProduct(product: ListedCrop?) {
        _selectedProduct.value = product
    }

    fun setCustomRate(rate: String) {
        _customRate.value = rate
    }

    fun getEffectiveRate(): Int {
        return _customRate.value.toIntOrNull() ?: _selectedProduct.value?.rate ?: 0
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUser = AuthHelper.getCurrentUserEmail()
                
                // Listen for real-time updates to the crops collection
                cropsCollection.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _error.value = "Error loading crops: ${error.message}"
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val cropsList = snapshot.documents.mapNotNull { doc ->
                            try {
                                val data = doc.data
                                if (data != null) {
                                    ListedCrop(
                                        id = doc.id,
                                        name = data["name"] as? String ?: "",
                                        quantity = (data["quantity"] as? Long)?.toInt() ?: 0,
                                        rate = (data["rate"] as? Long)?.toInt() ?: 0,
                                        location = data["location"] as? String ?: "",
                                        category = Category.valueOf(data["category"] as? String ?: Category.GRAINS.name),
                                        sellerName = data["sellerName"] as? String,
                                        sellerContact = data["sellerContact"] as? String,
                                        timestamp = null, // Firestore timestamp will be converted later if needed
                                        buyerDetails = emptyList(),
                                        isOwnListing = data["sellerContact"] == currentUser
                                    )
                                } else null
                            } catch (e: Exception) {
                                _error.value = "Error parsing crop data: ${e.message}"
                                null
                            }
                        }
                        _listedCrops.value = cropsList
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to load crops: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Function to add a new crop listing
    fun addCrop(
        name: String,
        quantity: Int,
        rate: Int,
        location: String,
        category: Category,
        sellerName: String,
        sellerContact: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val currentUser = AuthHelper.getCurrentUserEmail()
                val cropId = UUID.randomUUID().toString()
                
                // Create crop data for Firestore
                val cropData = mapOf(
                    "name" to name,
                    "quantity" to quantity,
                    "rate" to rate,
                    "location" to location,
                    "category" to category.name,
                    "sellerName" to sellerName,
                    "sellerContact" to sellerContact,
                    "timestamp" to com.google.firebase.Timestamp.now(),
                    "buyerDetails" to emptyList<String>()
                )
                
                // Add to Firestore
                cropsCollection.document(cropId).set(cropData).await()
                
                // Update local state
                val newCrop = ListedCrop(
                    id = cropId,
                    name = name,
                    quantity = quantity,
                    rate = rate,
                    location = location,
                    sellerName = sellerName,
                    sellerContact = sellerContact,
                    category = category,
                    timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                    buyerDetails = emptyList(),
                    isOwnListing = true
                )
                
                val currentList = _listedCrops.value.toMutableList()
                currentList.add(newCrop)
                _listedCrops.value = currentList
                
            } catch (e: Exception) {
                _error.value = "Failed to add crop: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getFoodsForCategory(category: Category): List<String> {
        return _foodsByCategory.value[category] ?: emptyList()
    }

    fun getTodaysRate(foodItem: String): Int {
        // In a real app, this would fetch from an API or database
        return 100
    }

    fun selectCrop(cropId: String) {
        viewModelScope.launch {
            try {
                // Fetch from Firestore
                val cropDoc = cropsCollection.document(cropId).get().await()
                
                if (cropDoc.exists()) {
                    val data = cropDoc.data
                    if (data != null) {
                        val currentUser = AuthHelper.getCurrentUserEmail()
                        
                        val crop = ListedCrop(
                            id = cropDoc.id,
                            name = data["name"] as? String ?: "",
                            quantity = (data["quantity"] as? Long)?.toInt() ?: 0,
                            rate = (data["rate"] as? Long)?.toInt() ?: 0,
                            location = data["location"] as? String ?: "",
                            category = Category.valueOf(data["category"] as? String ?: Category.GRAINS.name),
                            sellerName = data["sellerName"] as? String,
                            sellerContact = data["sellerContact"] as? String,
                            timestamp = null,
                            buyerDetails = emptyList(),
                            isOwnListing = data["sellerContact"] == currentUser
                        )
                        
                        _selectedCrop.value = crop
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to select crop: ${e.message}"
            }
        }
    }
    
    fun clearSelectedCrop() {
        _selectedCrop.value = null
    }
    
    fun sendPurchaseRequest(cropId: String, buyerDetail: BuyerDetail) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // Find the crop in Firestore
                val cropRef = cropsCollection.document(cropId)
                val cropDoc = cropRef.get().await()
                
                if (cropDoc.exists()) {
                    val currentQuantity = (cropDoc.get("quantity") as? Long)?.toInt() ?: 0
                    val newQuantity = currentQuantity - buyerDetail.requestedQuantity
                    
                    // Get current buyer details
                    val currentBuyerDetails = (cropDoc.get("buyerDetails") as? List<Map<String, Any>>) ?: emptyList()
                    
                    // Create new buyer detail map
                    val newBuyerDetail = mapOf(
                        "name" to buyerDetail.name,
                        "contactInfo" to buyerDetail.contactInfo,
                        "address" to buyerDetail.address,
                        "requestedQuantity" to buyerDetail.requestedQuantity
                    )
                    
                    // Update the document
                    cropRef.update(
                        mapOf(
                            "quantity" to newQuantity,
                            "buyerDetails" to (currentBuyerDetails + newBuyerDetail)
                        )
                    ).await()
                    
                    // Update local state
                    val cropIndex = _listedCrops.value.indexOfFirst { it.id == cropId }
                    if (cropIndex != -1) {
                        val crop = _listedCrops.value[cropIndex]
                        val updatedCrop = crop.copy(quantity = newQuantity)
                        val updatedList = _listedCrops.value.toMutableList()
                        updatedList[cropIndex] = updatedCrop
                        _listedCrops.value = updatedList
                        _selectedCrop.value = updatedCrop
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to send purchase request: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                // Fetch latest data from Firestore
                val snapshot = cropsCollection.get().await()
                val currentUser = AuthHelper.getCurrentUserEmail()
                
                val cropsList = snapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        if (data != null) {
                            ListedCrop(
                                id = doc.id,
                                name = data["name"] as? String ?: "",
                                quantity = (data["quantity"] as? Long)?.toInt() ?: 0,
                                rate = (data["rate"] as? Long)?.toInt() ?: 0,
                                location = data["location"] as? String ?: "",
                                category = Category.valueOf(data["category"] as? String ?: Category.GRAINS.name),
                                sellerName = data["sellerName"] as? String,
                                sellerContact = data["sellerContact"] as? String,
                                timestamp = null,
                                buyerDetails = emptyList(),
                                isOwnListing = data["sellerContact"] == currentUser
                            )
                        } else null
                    } catch (e: Exception) {
                        _error.value = "Error parsing crop data: ${e.message}"
                        null
                    }
                }
                _listedCrops.value = cropsList
            } catch (e: Exception) {
                _error.value = "Failed to refresh crops: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up resources if needed
    }
}

