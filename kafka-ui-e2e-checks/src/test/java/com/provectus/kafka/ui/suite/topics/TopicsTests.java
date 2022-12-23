package com.provectus.kafka.ui.suite.topics;

import static com.provectus.kafka.ui.pages.topic.TopicDetails.TopicMenu.MESSAGES;
import static com.provectus.kafka.ui.pages.topic.TopicDetails.TopicMenu.SETTINGS;
import static com.provectus.kafka.ui.pages.topic.enums.CleanupPolicyValue.COMPACT;
import static com.provectus.kafka.ui.pages.topic.enums.CleanupPolicyValue.DELETE;
import static com.provectus.kafka.ui.pages.topic.enums.CustomParameterType.COMPRESSION_TYPE;
import static com.provectus.kafka.ui.pages.topic.enums.MaxSizeOnDisk.SIZE_20_GB;
import static com.provectus.kafka.ui.settings.Source.CLUSTER_NAME;
import static com.provectus.kafka.ui.utilities.FileUtils.fileToString;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;

import com.codeborne.selenide.Condition;
import com.provectus.kafka.ui.base.BaseTest;
import com.provectus.kafka.ui.models.Topic;
import com.provectus.kafka.ui.pages.topic.TopicDetails;
import com.provectus.kafka.ui.utilities.qaseIoUtils.annotations.AutomationStatus;
import com.provectus.kafka.ui.utilities.qaseIoUtils.annotations.Suite;
import com.provectus.kafka.ui.utilities.qaseIoUtils.enums.Status;
import io.qameta.allure.Issue;
import io.qase.api.annotation.CaseId;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TopicsTests extends BaseTest {
  private static final long SUITE_ID = 2;
  private static final String SUITE_TITLE = "Topics";
  private static final Topic TOPIC_TO_CREATE = new Topic()
      .setName("new-topic-" + randomAlphabetic(5))
      .setNumberOfPartitions(1)
      .setCustomParameterType(COMPRESSION_TYPE)
      .setCustomParameterValue("producer")
      .setCleanupPolicyValue(DELETE);
  private static final Topic TOPIC_FOR_UPDATE = new Topic()
      .setName("topic-to-update-" + randomAlphabetic(5))
      .setCleanupPolicyValue(COMPACT)
      .setTimeToRetainData("604800001")
      .setMaxSizeOnDisk(SIZE_20_GB)
      .setMaxMessageBytes("1000020")
      .setMessageKey(fileToString(System.getProperty("user.dir") + "/src/test/resources/producedkey.txt"))
      .setMessageContent(fileToString(System.getProperty("user.dir") + "/src/test/resources/testData.txt"));

  private static final Topic TOPIC_FOR_DELETE = new Topic().setName("topic-to-delete-" + randomAlphabetic(5));
  private static final List<Topic> TOPIC_LIST = new ArrayList<>();

  @BeforeAll
  public void beforeAll() {
    TOPIC_LIST.addAll(List.of(TOPIC_FOR_UPDATE, TOPIC_FOR_DELETE));
    TOPIC_LIST.forEach(topic -> apiHelper.createTopic(CLUSTER_NAME, topic.getName()));
  }

  @DisplayName("should create a topic")
  @Suite(suiteId = 4, title = "Create new Topic")
  @AutomationStatus(status = Status.AUTOMATED)
  @CaseId(199)
  @Test
  @Order(1)
  public void createTopic() {
    navigateToTopics();
    topicsList
        .clickAddTopicBtn();
    topicCreateEditForm
        .waitUntilScreenReady()
        .setTopicName(TOPIC_TO_CREATE.getName())
        .setNumberOfPartitions(TOPIC_TO_CREATE.getNumberOfPartitions())
        .selectCleanupPolicy(TOPIC_TO_CREATE.getCleanupPolicyValue())
        .clickCreateTopicBtn();
    navigateToTopicsAndOpenDetails(TOPIC_TO_CREATE.getName());
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(topicDetails.isTopicHeaderVisible(TOPIC_TO_CREATE.getName())).as("isTopicHeaderVisible()")
        .isTrue();
    softly.assertThat(topicDetails.getCleanUpPolicy()).as("getCleanUpPolicy()")
        .isEqualTo(TOPIC_TO_CREATE.getCleanupPolicyValue().toString());
    softly.assertThat(topicDetails.getPartitions()).as("getPartitions()")
        .isEqualTo(TOPIC_TO_CREATE.getNumberOfPartitions());
    softly.assertAll();
    navigateToTopics();
    Assertions.assertTrue(topicsList.isTopicVisible(TOPIC_TO_CREATE.getName()), "isTopicVisible");
    TOPIC_LIST.add(TOPIC_TO_CREATE);
  }

  @DisplayName("Checking available operations for selected Topic within 'All Topics' page")
  @Suite(suiteId = SUITE_ID, title = SUITE_TITLE)
  @AutomationStatus(status = Status.AUTOMATED)
  @CaseId(7)
  @Test
  @Order(2)
  void checkAvailableOperations() {
    navigateToTopics();
    topicsList
        .getTopicItem("my_ksql_1ksql_processing_log")
        .selectItem(true);
    verifyElementsCondition(topicsList.getActionButtons(),Condition.enabled);
    topicsList
        .getTopicItem("_confluent-ksql-my_ksql_1_command_topic")
        .selectItem(true);
    Assertions.assertFalse(topicsList.isCopySelectedTopicBtnEnabled(), "isCopySelectedTopicBtnEnabled()");
  }

  @Disabled()
  @Issue("https://github.com/provectus/kafka-ui/issues/2625")
  @DisplayName("should update a topic")
  @Suite(suiteId = SUITE_ID, title = SUITE_TITLE)
  @AutomationStatus(status = Status.AUTOMATED)
  @CaseId(197)
  @Test
  @Order(3)
  public void updateTopic() {
    navigateToTopicsAndOpenDetails(TOPIC_FOR_UPDATE.getName());
    topicDetails
        .openDotMenu()
        .clickEditSettingsMenu();
    topicCreateEditForm
        .waitUntilScreenReady()
        .selectCleanupPolicy((TOPIC_FOR_UPDATE.getCleanupPolicyValue()))
        .setMinInsyncReplicas(10)
        .setTimeToRetainDataInMs(TOPIC_FOR_UPDATE.getTimeToRetainData())
        .setMaxSizeOnDiskInGB(TOPIC_FOR_UPDATE.getMaxSizeOnDisk())
        .setMaxMessageBytes(TOPIC_FOR_UPDATE.getMaxMessageBytes())
        .clickCreateTopicBtn();
    topicDetails
        .waitUntilScreenReady();
    navigateToTopicsAndOpenDetails(TOPIC_FOR_UPDATE.getName());
    topicDetails
        .openDotMenu()
        .clickEditSettingsMenu();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(topicCreateEditForm.getCleanupPolicy()).as("getCleanupPolicy()")
        .isEqualTo(TOPIC_FOR_UPDATE.getCleanupPolicyValue().getVisibleText());
    softly.assertThat(topicCreateEditForm.getTimeToRetain()).as("getTimeToRetain()")
        .isEqualTo(TOPIC_FOR_UPDATE.getTimeToRetainData());
    softly.assertThat(topicCreateEditForm.getMaxSizeOnDisk()).as("getMaxSizeOnDisk()")
        .isEqualTo(TOPIC_FOR_UPDATE.getMaxSizeOnDisk().getVisibleText());
    softly.assertThat(topicCreateEditForm.getMaxMessageBytes()).as("getMaxMessageBytes()")
        .isEqualTo(TOPIC_FOR_UPDATE.getMaxMessageBytes());
    softly.assertAll();
  }

  @DisplayName("should delete topic")
  @Suite(suiteId = SUITE_ID, title = SUITE_TITLE)
  @AutomationStatus(status = Status.AUTOMATED)
  @CaseId(207)
  @Test
  @Order(4)
  public void deleteTopic() {
    navigateToTopicsAndOpenDetails(TOPIC_FOR_DELETE.getName());
    topicDetails
        .openDotMenu()
        .clickDeleteTopicMenu()
        .clickConfirmDeleteBtn();
    navigateToTopics();
    Assertions.assertFalse(topicsList.isTopicVisible(TOPIC_FOR_DELETE.getName()), "isTopicVisible");
    TOPIC_LIST.remove(TOPIC_FOR_DELETE);
  }

  @DisplayName("Redirect to consumer from topic profile")
  @Suite(suiteId = SUITE_ID, title = SUITE_TITLE)
  @AutomationStatus(status = Status.AUTOMATED)
  @CaseId(20)
  @Test
  @Order(5)
  void redirectToConsumerFromTopic() {
    String topicName = "source-activities";
    String consumerGroupId = "connect-sink_postgres_activities";
    navigateToTopicsAndOpenDetails(topicName);
    topicDetails
        .openDetailsTab(TopicDetails.TopicMenu.CONSUMERS)
        .openConsumerGroup(consumerGroupId);
    consumersDetails
        .waitUntilScreenReady();
    assertThat(consumersDetails.isRedirectedConsumerTitleVisible(consumerGroupId))
        .withFailMessage("isRedirectedConsumerTitleVisible").isTrue();
    assertThat(consumersDetails.isTopicInConsumersDetailsVisible(topicName))
        .withFailMessage("isTopicInConsumersDetailsVisible").isTrue();
  }

  @DisplayName("Checking Topic creation possibility in case of empty Topic Name")
  @Suite(suiteId = SUITE_ID, title = SUITE_TITLE)
  @AutomationStatus(status = Status.AUTOMATED)
  @CaseId(4)
  @Test
  @Order(6)
  void checkTopicCreatePossibility() {
    navigateToTopics();
    topicsList
        .clickAddTopicBtn();
    topicCreateEditForm
        .waitUntilScreenReady();
    assertThat(topicCreateEditForm.isCreateTopicButtonEnabled()).as("isCreateTopicButtonEnabled()").isFalse();
    topicCreateEditForm
        .setTopicName("testName");
    assertThat(topicCreateEditForm.isCreateTopicButtonEnabled()).as("isCreateTopicButtonEnabled()").isFalse();
    topicCreateEditForm
        .setTopicName(null)
        .setNumberOfPartitions(nextInt(1, 10));
    assertThat(topicCreateEditForm.isCreateTopicButtonEnabled()).as("isCreateTopicButtonEnabled()").isFalse();
    topicCreateEditForm
        .setTopicName("testName");
    assertThat(topicCreateEditForm.isCreateTopicButtonEnabled()).as("isCreateTopicButtonEnabled()").isTrue();
  }

  @DisplayName("Checking 'Time to retain data (in ms)' custom value with editing Topic's settings")
  @Suite(suiteId = SUITE_ID, title = SUITE_TITLE)
  @AutomationStatus(status = Status.AUTOMATED)
  @CaseId(266)
  @Test
  @Order(7)
  void checkTimeToRetainDataCustomValueWithEditingTopic() {
    Topic topicToRetainData = new Topic()
        .setName("topic-to-retain-data-" + randomAlphabetic(5))
        .setTimeToRetainData("86400000");
    navigateToTopics();
    topicsList
        .clickAddTopicBtn();
    topicCreateEditForm
        .waitUntilScreenReady()
        .setTopicName(topicToRetainData.getName())
        .setNumberOfPartitions(1)
        .setTimeToRetainDataInMs("604800000");
    assertThat(topicCreateEditForm.getTimeToRetain()).as("getTimeToRetain()").isEqualTo("604800000");
    topicCreateEditForm
        .setTimeToRetainDataInMs(topicToRetainData.getTimeToRetainData())
        .clickCreateTopicBtn();
    topicDetails
        .waitUntilScreenReady()
        .openDotMenu()
        .clickEditSettingsMenu();
    assertThat(topicCreateEditForm.getTimeToRetain()).as("getTimeToRetain()")
        .isEqualTo(topicToRetainData.getTimeToRetainData());
    topicDetails
        .openDetailsTab(SETTINGS);
    assertThat(topicDetails.getSettingsGridValueByKey("retention.ms")).as("getSettingsGridValueByKey()")
        .isEqualTo(topicToRetainData.getTimeToRetainData());
    TOPIC_LIST.add(topicToRetainData);
  }

  @DisplayName("Checking requiredness of Custom parameters within 'Create new Topic'")
  @Suite(suiteId = SUITE_ID, title = SUITE_TITLE)
  @AutomationStatus(status = Status.AUTOMATED)
  @CaseId(6)
  @Test
  @Order(8)
  void checkCustomParametersWithinCreateNewTopic() {
    navigateToTopics();
    topicsList
        .waitUntilScreenReady()
        .clickAddTopicBtn();
    topicCreateEditForm
        .waitUntilScreenReady()
        .setTopicName(TOPIC_TO_CREATE.getName())
        .clickAddCustomParameterTypeButton()
        .setCustomParameterType(TOPIC_TO_CREATE.getCustomParameterType());
    assertThat(topicCreateEditForm.isDeleteCustomParameterButtonEnabled()).as("isDeleteCustomParameterButtonEnabled()")
        .isTrue();
    topicCreateEditForm
        .clearCustomParameterValue();
    assertThat(topicCreateEditForm.isValidationMessageCustomParameterValueVisible())
        .as("isValidationMessageCustomParameterValueVisible()").isTrue();
  }

  @DisplayName("Checking Topics section within Kafka-ui Application")
  @Suite(suiteId = SUITE_ID, title = SUITE_TITLE)
  @AutomationStatus(status = Status.AUTOMATED)
  @CaseId(2)
  @Test
  @Order(9)
  void checkTopicListElements() {
    navigateToTopics();
    verifyElementsCondition(topicsList.getAllVisibleElements(), Condition.visible);
    verifyElementsCondition(topicsList.getAllEnabledElements(), Condition.enabled);
  }

  @DisplayName("Filter adding within Topic")
  @Suite(suiteId = SUITE_ID, title = SUITE_TITLE)
  @AutomationStatus(status = Status.AUTOMATED)
  @CaseId(12)
  @Test
  @Order(10)
  void addingNewFilterWithinTopic() {
    String filterName = randomAlphabetic(5);
    navigateToTopicsAndOpenDetails("_schemas");
    topicDetails
        .openDetailsTab(MESSAGES)
        .clickMessagesAddFiltersBtn()
        .waitUntilAddFiltersMdlVisible();
    verifyElementsCondition(topicDetails.getAllAddFilterModalVisibleElements(), Condition.visible);
    verifyElementsCondition(topicDetails.getAllAddFilterModalEnabledElements(), Condition.enabled);
    verifyElementsCondition(topicDetails.getAllAddFilterModalDisabledElements(), Condition.disabled);
    assertThat(topicDetails.isSaveThisFilterCheckBoxSelected()).as("isSaveThisFilterCheckBoxSelected()")
        .isFalse();
    topicDetails
        .setFilterCodeFieldAddFilterMdl(filterName);
    assertThat(topicDetails.isAddFilterBtnAddFilterMdlEnabled()).as("isAddFilterBtnAddFilterMdlEnabled()")
        .isTrue();
    topicDetails.clickAddFilterBtnAndCloseMdl(true);
    assertThat(topicDetails.isActiveFilterVisible(filterName)).as("isActiveFilterVisible()")
        .isTrue();
  }

  @DisplayName("Checking filter saving within Messages/Topic profile/Saved Filters")
  @Suite(suiteId = SUITE_ID, title = SUITE_TITLE)
  @AutomationStatus(status = Status.AUTOMATED)
  @CaseId(13)
  @Test
  @Order(11)
  void checkFilterSavingWithinSavedFilters() {
    String displayName = randomAlphabetic(5);
    navigateToTopicsAndOpenDetails("my_ksql_1ksql_processing_log");
    topicDetails
        .openDetailsTab(MESSAGES)
        .clickMessagesAddFiltersBtn()
        .waitUntilAddFiltersMdlVisible()
        .setFilterCodeFieldAddFilterMdl(randomAlphabetic(4))
        .selectSaveThisFilterCheckboxMdl(true)
        .setDisplayNameFldAddFilterMdl(displayName);
    assertThat(topicDetails.isAddFilterBtnAddFilterMdlEnabled()).as("isAddFilterBtnAddFilterMdlEnabled()")
        .isTrue();
    topicDetails
        .clickAddFilterBtnAndCloseMdl(false)
        .openSavedFiltersListMdl();
    assertThat(topicDetails.isFilterVisibleAtSavedFiltersMdl(displayName))
        .as("isFilterVisibleAtSavedFiltersMdl()").isTrue();
  }

  @DisplayName("Checking applying saved filter within Topic/Messages")
  @Suite(suiteId = SUITE_ID, title = SUITE_TITLE)
  @AutomationStatus(status = Status.AUTOMATED)
  @CaseId(14)
  @Test
  @Order(12)
  void checkingApplyingSavedFilterWithinTopicMessages() {
    String displayName = randomAlphabetic(5);
    navigateToTopicsAndOpenDetails("my_ksql_1ksql_processing_log");
    topicDetails
        .openDetailsTab(MESSAGES)
        .clickMessagesAddFiltersBtn()
        .waitUntilAddFiltersMdlVisible()
        .setFilterCodeFieldAddFilterMdl(randomAlphabetic(4))
        .selectSaveThisFilterCheckboxMdl(true)
        .setDisplayNameFldAddFilterMdl(displayName)
        .clickAddFilterBtnAndCloseMdl(false)
        .openSavedFiltersListMdl()
        .selectFilterAtSavedFiltersMdl(displayName)
        .clickSelectFilterBtnAtSavedFiltersMdl();
    assertThat(topicDetails.isActiveFilterVisible(displayName))
        .as("isActiveFilterVisible()").isTrue();
  }

  @DisplayName("Checking 'Show Internal Topics' toggle functionality within 'All Topics' page")
  @Suite(suiteId = SUITE_ID, title = SUITE_TITLE)
  @AutomationStatus(status = Status.AUTOMATED)
  @CaseId(11)
  @Test
  @Order(13)
  void checkShowInternalTopicsButtonFunctionality(){
    navigateToTopics();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(topicsList.isShowInternalRadioBtnSelected()).as("isInternalRadioBtnSelected()").isTrue();
    softly.assertThat(topicsList.getInternalTopics()).as("getInternalTopics()").size().isGreaterThan(0);
    softly.assertThat(topicsList.getNonInternalTopics()).as("getNonInternalTopics()").size().isGreaterThan(0);
    softly.assertAll();
    topicsList
        .setShowInternalRadioButton(false);
    softly.assertThat(topicsList.getInternalTopics()).as("getInternalTopics()").size().isEqualTo(0);
    softly.assertThat(topicsList.getNonInternalTopics()).as("getNonInternalTopics()").size().isGreaterThan(0);
    softly.assertAll();
  }

  @AfterAll
  public void afterAll() {
    TOPIC_LIST.forEach(topic -> apiHelper.deleteTopic(CLUSTER_NAME, topic.getName()));
  }
}