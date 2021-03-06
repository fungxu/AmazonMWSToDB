package com.lokivog.mws.products;

import static com.lokivog.mws.Constants.FEATURE;
import static com.lokivog.mws.Constants.OUTPUT_DIR;
import static com.lokivog.mws.products.ProductMain.JSON_LOGGER;
import static com.lokivog.mws.products.ProductMain.XML_LOGGER;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amazonservices.mws.products.MarketplaceWebServiceProducts;
import com.amazonservices.mws.products.MarketplaceWebServiceProductsClient;
import com.amazonservices.mws.products.MarketplaceWebServiceProductsException;
import com.amazonservices.mws.products.mock.MarketplaceWebServiceProductsMock;
import com.amazonservices.mws.products.model.ASINIdentifier;
import com.amazonservices.mws.products.model.AttributeSetList;
import com.amazonservices.mws.products.model.CompetitivePriceList;
import com.amazonservices.mws.products.model.CompetitivePriceType;
import com.amazonservices.mws.products.model.CompetitivePricingType;
import com.amazonservices.mws.products.model.GetMatchingProductForIdRequest;
import com.amazonservices.mws.products.model.GetMatchingProductForIdResponse;
import com.amazonservices.mws.products.model.GetMatchingProductForIdResult;
import com.amazonservices.mws.products.model.IdListType;
import com.amazonservices.mws.products.model.IdentifierType;
import com.amazonservices.mws.products.model.LowestOfferListingList;
import com.amazonservices.mws.products.model.LowestOfferListingType;
import com.amazonservices.mws.products.model.MoneyType;
import com.amazonservices.mws.products.model.NumberOfOfferListingsList;
import com.amazonservices.mws.products.model.OfferListingCountType;
import com.amazonservices.mws.products.model.OfferType;
import com.amazonservices.mws.products.model.OffersList;
import com.amazonservices.mws.products.model.PriceType;
import com.amazonservices.mws.products.model.Product;
import com.amazonservices.mws.products.model.ProductList;
import com.amazonservices.mws.products.model.QualifiersType;
import com.amazonservices.mws.products.model.RelationshipList;
import com.amazonservices.mws.products.model.ResponseMetadata;
import com.amazonservices.mws.products.model.SalesRankList;
import com.amazonservices.mws.products.model.SalesRankType;
import com.amazonservices.mws.products.model.SellerSKUIdentifier;
import com.amazonservices.mws.products.model.ShippingTimeType;
import com.amazonservices.mws.products.samples.ProductsConfig;
import com.lokivog.mws.Constants;

// IMPORTANT: The Amazon MWS Java Client API is required to run this class. Download from the Amazon developer website. Once downloaded
// add the client library to the classpath
/**
 * The Class GetMatchingProductForId. Calls the Amazon MWS Product Feed API to retrieve a list of products matching a list of UPC ids.
 */
public class GetMatchingProductForId {

	final Logger logger = LoggerFactory.getLogger(GetMatchingProductForId.class);

	private List<String> mProductIds;

	private Date mStartDate = new Date();

	// private String mIdType = "UPC";
	private String mIdType = "UPC";

	private static boolean mRequestsThrottled = false;

	public GetMatchingProductForId(String pId) {
		createOutputDir();
	}

	public GetMatchingProductForId(List<String> pProductIds) {
		mProductIds = pProductIds;
		createOutputDir();
	}

	public GetMatchingProductForId(List<String> pProductIds, Date pStartDate, String pIdType) {
		mProductIds = pProductIds;
		mStartDate = pStartDate;
		mIdType = pIdType;
		createOutputDir();
	}

	public JSONArray matchProducts() {
		MarketplaceWebServiceProducts service;
		boolean mock = false;
		if (mock) {
			service = new MarketplaceWebServiceProductsMock();
		} else {
			service = new MarketplaceWebServiceProductsClient(ProductsConfig.accessKeyId,
					ProductsConfig.secretAccessKey, ProductsConfig.applicationName, ProductsConfig.applicationVersion,
					ProductsConfig.config);
		}

		/************************************************************************
		 * Setup request parameters and uncomment invoke to try out sample for Get Matching Product For Id
		 ***********************************************************************/
		GetMatchingProductForIdRequest request = new GetMatchingProductForIdRequest();
		request.setSellerId(ProductsConfig.sellerId);
		request.setMarketplaceId(ProductsConfig.marketplaceId);
		IdListType idListType = new IdListType();
		idListType.setId(mProductIds);
		logger.debug("Looking up UPC products: {}", mProductIds);
		request.setIdList(idListType);
		request.setIdType(getIdType());
		JSONArray response = null;
		try {
			response = invokeGetMatchingProductForId(service, request);
			// generateElasticSearchJson(response);
		} catch (Exception e) {
			logger.error("exception connection to elasticsearch", e);
		}
		return response;
	}

	private void createOutputDir() {
		File theDir = new File(OUTPUT_DIR);

		// if the directory does not exist, create it
		if (!theDir.exists()) {
			logger.info("creating directory: {} ", OUTPUT_DIR);
			boolean result = theDir.mkdir();
			if (result) {
				logger.info("{} created", OUTPUT_DIR);
			}
		}
	}

	/**
	 * Just add few required parameters, and try the service Get Matching Product For Id functionality
	 * 
	 * @param args unused
	 */
	public static void main(String... args) {
		List<String> ids = new ArrayList<String>();
		ids.add("691374334695");
		// ids.add("5250T");
		// ids.add("15218640");
		// ids.add("5250T~B11530087");
		// ids.add("B11530087");
		// ids.add("39001SAMSILL");
		GetMatchingProductForId matchingProductForId = new GetMatchingProductForId(ids);
		System.out.println("product: " + matchingProductForId.test());
	}

	public Object test() {
		return matchProducts();
		// List<GetMatchingProductForIdResult> matchingProductForIdResultList = response
		// .getGetMatchingProductForIdResult();
		// logger.info(response.toJSON());
		// JSONObject object = new JSONObject(response.toJSON());
		// JSONObject jsonResponse = object.getJSONObject("GetMatchingProductForIdResponse");
		// JSONArray jsonResult = jsonResponse.getJSONArray("GetMatchingProductForIdResult");
		// JSONObject products = jsonResult.getJSONObject(0);
	}

	public void generateElasticSearchJson(GetMatchingProductForIdResponse pResponse) {
		List<GetMatchingProductForIdResult> matchingProductForIdResultList = pResponse
				.getGetMatchingProductForIdResult();
		StringBuilder builder = new StringBuilder();
		for (GetMatchingProductForIdResult getMatchingProductForIdResult : matchingProductForIdResultList) {
			ProductList products = getMatchingProductForIdResult.getProducts();
			java.util.List<Product> productList = products.getProduct();
			for (Product product : productList) {
				builder.append("MarketplaceASIN = " + product.getIdentifiers().getMarketplaceASIN());
			}
		}
		logger.debug("builder=" + builder.toString());
	}

	/**
	 * Get Matching Product For Id request sample GetMatchingProduct will return the details (attributes) for the given Identifier list.
	 * Identifer type can be one of [SKU|ASIN|UPC|EAN|ISBN|GTIN|JAN]
	 * 
	 * @param service instance of MarketplaceWebServiceProducts service
	 * @param request Action to invoke
	 */
	public JSONArray invokeGetMatchingProductForId(MarketplaceWebServiceProducts service,
			GetMatchingProductForIdRequest request) {
		GetMatchingProductForIdResponse response = null;
		JSONArray productArray = new JSONArray();
		try {
			if (isRequestsThrottled()) {
				Thread.sleep(Constants.THROTTLE_MS_GET_MATCHING_PRODUCT_FOR_ID);
			}
			response = service.getMatchingProductForId(request);
			logger.debug("GetMatchingProductForId Action Response");
			List<GetMatchingProductForIdResult> getMatchingProductForIdResultList = response
					.getGetMatchingProductForIdResult();
			// logger.info("response: \n" + response.toXML());
			XML_LOGGER.info(response.toXML());
			// logger.info(response.toJSON());
			// Iterates over the Amazon response and builds a json object from the xml response.
			// This doesn't set all the properties from the xml schema object but it does contain place holders to set them in the future
			// if needed
			String requestId = null;
			if (response.isSetResponseMetadata()) {
				ResponseMetadata responseMetadata = response.getResponseMetadata();
				if (responseMetadata.isSetRequestId()) {
					requestId = responseMetadata.getRequestId();
					logger.debug("responseMetadata requestId {}", responseMetadata.getRequestId());
				}
			}
			for (GetMatchingProductForIdResult getMatchingProductForIdResult : getMatchingProductForIdResultList) {
				JSONObject object = new JSONObject();
				if (getMatchingProductForIdResult.isSetId()) {
					object.put(Constants.ID, getMatchingProductForIdResult.getId());
				}
				if (getMatchingProductForIdResult.isSetIdType()) {
					object.put(Constants.ID_TYPE, getMatchingProductForIdResult.getIdType());
				}
				if (getMatchingProductForIdResult.isSetStatus()) {
					object.put(Constants.STATUS, getMatchingProductForIdResult.getStatus());
				}
				if (getMatchingProductForIdResult.isSetProducts()) {
					ProductList products = getMatchingProductForIdResult.getProducts();
					JSONArray array = new JSONArray();
					java.util.List<Product> productList = products.getProduct();
					for (Product product : productList) {
						JSONObject jsonProduct = new JSONObject();

						if (product.isSetIdentifiers()) {
							IdentifierType identifiers = product.getIdentifiers();

							if (identifiers.isSetMarketplaceASIN()) {
								ASINIdentifier marketplaceASIN = identifiers.getMarketplaceASIN();
								if (marketplaceASIN.isSetMarketplaceId()) {
									jsonProduct.put(Constants.MARKET_PLACE_ID, marketplaceASIN.getMarketplaceId());
								}
								if (marketplaceASIN.isSetASIN()) {
									jsonProduct.put(Constants.ASIN, marketplaceASIN.getASIN());
								}
							}
							if (identifiers.isSetSKUIdentifier()) {
								SellerSKUIdentifier SKUIdentifier = identifiers.getSKUIdentifier();
								if (SKUIdentifier.isSetMarketplaceId()) {
								}
								if (SKUIdentifier.isSetSellerId()) {
									jsonProduct.put("sellerId", SKUIdentifier.getSellerId());
								}
								if (SKUIdentifier.isSetSellerSKU()) {
									jsonProduct.put("sellerSKU", SKUIdentifier.getSellerSKU());
								}
							}

						}
						if (product.isSetAttributeSets()) {
							AttributeSetList attributeSetList = product.getAttributeSets();
							for (Object obj : attributeSetList.getAny()) {
								Node attribute = (Node) obj;
								NodeList nodeList = attribute.getChildNodes();
								for (int i = 0; i < nodeList.getLength(); i++) {
									String nodeName = nodeList.item(i).getNodeName();
									nodeName = nodeName.replaceFirst("ns2:", "");
									Node myNode = nodeList.item(i);

									if (nodeName.equals(FEATURE)) {
										if (jsonProduct.has(nodeName)) {
											String feature = jsonProduct.getString(nodeName);
											feature = feature + ", " + nodeList.item(i).getTextContent();
											jsonProduct.put(nodeName, feature);
										} else {
											jsonProduct.put(nodeName, nodeList.item(i).getTextContent());
										}

									} else if (nodeName.equals("PackageDimensions")) {
										NodeList packageDimNodeList = myNode.getChildNodes();
										for (int j = 0; j < packageDimNodeList.getLength(); j++) {
											Node packageDimNode = packageDimNodeList.item(j);
											if (!StringUtils.isEmpty(packageDimNode.getTextContent())) {
												String packageDimName = packageDimNode.getNodeName();
												packageDimName = packageDimName.replaceFirst("ns2:", "");
												jsonProduct.put("Package" + packageDimName,
														packageDimNode.getTextContent());
											}

										}
										jsonProduct.put(nodeName, nodeList.item(i).getTextContent());
									} else {
										jsonProduct.put(nodeName, nodeList.item(i).getTextContent());
									}
									// logger.info("nodeName: {}={}", nodeName, nodeList.item(i).getTextContent());

								}
							}
						}
						if (product.isSetRelationships()) {
							RelationshipList relationships = product.getRelationships();
							for (Object obj : relationships.getAny()) {
								Node relationship = (Node) obj;
								// System.out.println(ProductsUtil.formatXml(relationship));
							}
						}
						if (product.isSetCompetitivePricing()) {
							CompetitivePricingType competitivePricing = product.getCompetitivePricing();
							if (competitivePricing.isSetCompetitivePrices()) {
								CompetitivePriceList competitivePrices = competitivePricing.getCompetitivePrices();
								java.util.List<CompetitivePriceType> competitivePriceList = competitivePrices
										.getCompetitivePrice();
								// logger.info("competitivePriceList: " + competitivePriceList);
								for (CompetitivePriceType competitivePrice : competitivePriceList) {
									if (competitivePrice.isSetCondition()) {
									}
									if (competitivePrice.isSetSubcondition()) {
									}
									if (competitivePrice.isSetBelongsToRequester()) {
									}
									if (competitivePrice.isSetCompetitivePriceId()) {
									}
									if (competitivePrice.isSetPrice()) {
										PriceType price = competitivePrice.getPrice();
										if (price.isSetLandedPrice()) {
											MoneyType landedPrice = price.getLandedPrice();
											if (landedPrice.isSetCurrencyCode()) {
											}
											if (landedPrice.isSetAmount()) {
											}
										}
										if (price.isSetListingPrice()) {
											MoneyType listingPrice = price.getListingPrice();
											if (listingPrice.isSetCurrencyCode()) {
											}
											if (listingPrice.isSetAmount()) {
											}
										}
										if (price.isSetShipping()) {
											MoneyType shipping = price.getShipping();
											if (shipping.isSetCurrencyCode()) {
											}
											if (shipping.isSetAmount()) {

											}
										}
									}
								}
							}
							if (competitivePricing.isSetNumberOfOfferListings()) {
								NumberOfOfferListingsList numberOfOfferListings = competitivePricing
										.getNumberOfOfferListings();
								java.util.List<OfferListingCountType> offerListingCountList = numberOfOfferListings
										.getOfferListingCount();
								for (OfferListingCountType offerListingCount : offerListingCountList) {
									if (offerListingCount.isSetCondition()) {
									}
									if (offerListingCount.isSetValue()) {
									}
								}
							}
							if (competitivePricing.isSetTradeInValue()) {
								MoneyType tradeInValue = competitivePricing.getTradeInValue();
								if (tradeInValue.isSetCurrencyCode()) {
								}
								if (tradeInValue.isSetAmount()) {
								}
							}
						}
						if (product.isSetSalesRankings()) {
							SalesRankList salesRankings = product.getSalesRankings();
							java.util.List<SalesRankType> salesRankList = salesRankings.getSalesRank();
							for (SalesRankType salesRank : salesRankList) {
								if (salesRank.isSetProductCategoryId()) {
								}
								if (salesRank.isSetRank()) {
								}
							}
						}
						if (product.isSetLowestOfferListings()) {
							LowestOfferListingList lowestOfferListings = product.getLowestOfferListings();
							java.util.List<LowestOfferListingType> lowestOfferListingList = lowestOfferListings
									.getLowestOfferListing();
							for (LowestOfferListingType lowestOfferListing : lowestOfferListingList) {
								if (lowestOfferListing.isSetQualifiers()) {
									QualifiersType qualifiers = lowestOfferListing.getQualifiers();
									if (qualifiers.isSetItemCondition()) {
									}
									if (qualifiers.isSetItemSubcondition()) {
									}
									if (qualifiers.isSetFulfillmentChannel()) {
									}
									if (qualifiers.isSetShipsDomestically()) {
									}
									if (qualifiers.isSetShippingTime()) {
										ShippingTimeType shippingTime = qualifiers.getShippingTime();
										if (shippingTime.isSetMax()) {
										}
									}
									if (qualifiers.isSetSellerPositiveFeedbackRating()) {
									}
								}
								if (lowestOfferListing.isSetNumberOfOfferListingsConsidered()) {
								}
								if (lowestOfferListing.isSetSellerFeedbackCount()) {
								}
								if (lowestOfferListing.isSetPrice()) {
									PriceType price1 = lowestOfferListing.getPrice();
									if (price1.isSetLandedPrice()) {
										MoneyType landedPrice1 = price1.getLandedPrice();
										if (landedPrice1.isSetCurrencyCode()) {
										}
										if (landedPrice1.isSetAmount()) {
										}
									}
									if (price1.isSetListingPrice()) {
										MoneyType listingPrice1 = price1.getListingPrice();
										if (listingPrice1.isSetCurrencyCode()) {
										}
										if (listingPrice1.isSetAmount()) {
										}
									}
									if (price1.isSetShipping()) {
										MoneyType shipping1 = price1.getShipping();
										if (shipping1.isSetCurrencyCode()) {
										}
										if (shipping1.isSetAmount()) {
										}
									}
								}
								if (lowestOfferListing.isSetMultipleOffersAtLowestPrice()) {
								}
							}
						}
						if (product.isSetOffers()) {
							OffersList offers = product.getOffers();
							java.util.List<OfferType> offerList = offers.getOffer();
							for (OfferType offer : offerList) {
								if (offer.isSetBuyingPrice()) {
									PriceType buyingPrice = offer.getBuyingPrice();
									if (buyingPrice.isSetLandedPrice()) {
										MoneyType landedPrice2 = buyingPrice.getLandedPrice();
										if (landedPrice2.isSetCurrencyCode()) {
										}
										if (landedPrice2.isSetAmount()) {
										}
									}
									if (buyingPrice.isSetListingPrice()) {
										MoneyType listingPrice2 = buyingPrice.getListingPrice();
										if (listingPrice2.isSetCurrencyCode()) {
										}
										if (listingPrice2.isSetAmount()) {
										}
									}
									if (buyingPrice.isSetShipping()) {
										MoneyType shipping2 = buyingPrice.getShipping();
										if (shipping2.isSetCurrencyCode()) {
										}
										if (shipping2.isSetAmount()) {
										}
									}
								}
								if (offer.isSetRegularPrice()) {
									MoneyType regularPrice = offer.getRegularPrice();
									if (regularPrice.isSetCurrencyCode()) {
									}
									if (regularPrice.isSetAmount()) {
									}
								}
								if (offer.isSetFulfillmentChannel()) {
								}
								if (offer.isSetItemCondition()) {
								}
								if (offer.isSetItemSubCondition()) {
								}
								if (offer.isSetSellerId()) {
								}
								if (offer.isSetSellerSKU()) {
								}
							}
						}
						array.put(jsonProduct);
					}
					object.put(Constants.PRODUCTS, array);
					// processJSON(object);
					// insertJSONIntoDB(object);
					// logger.info("jsonProduct: " + object);
				}
				if (getMatchingProductForIdResult.isSetError()) {
					com.amazonservices.mws.products.model.Error error = getMatchingProductForIdResult.getError();
					logger.error("Amazon Response Error - Type: {}, Code: {}, Message: {}", error.getType(),
							error.getCode(), error.getMessage());
					object.put("errorType", error.getType());
					object.put("errorCode", error.getCode());
					object.put("errorMessage", error.getMessage());
				}
				if (requestId != null) {
					object.put("requestId", requestId);
				}
				productArray.put(object);
				JSON_LOGGER.info(object.toString());
			}

		} catch (MarketplaceWebServiceProductsException ex) {
			logger.error("Caught Exception: ", ex.getMessage());
			logger.error("Response Status Code: {}", ex.getStatusCode());
			logger.error("Error Code: {}", ex.getErrorCode());
			logger.error("Error Type: {}", ex.getErrorType());
			logger.error("Request ID: {}", ex.getRequestId());
			logger.error("XML: {}", ex.getXML());
			logger.error("ResponseHeaderMetadata: {}", ex.getResponseHeaderMetadata());
			if (ex.getErrorCode() != null) {
				if (ex.getErrorCode().equals("RequestThrottled")) {
					setRequestsThrottled(true);
				}
			}
		} catch (InterruptedException e) {
			logger.error("InterruptedException putting thread to sleep when throttling", e);
		}
		return productArray;
	}

	private Set<String> allNames = new HashSet<String>();

	public void processJSON(JSONObject pObject) {
		JSONArray names = pObject.names();
		logger.info("names: " + names);
		JSONArray products = pObject.getJSONArray("products");
		int size = products.length();

		for (int i = 0; i < size; i++) {
			JSONObject obj = products.getJSONObject(i);
			JSONArray prodNames = obj.names();
			int prodSize = prodNames.length();
			for (int j = 0; j < prodSize; j++) {
				String name = prodNames.getString(j).toUpperCase();
				String field = String.format(
						"public static final SFieldString %s = new SFieldString(PRODUCT, \"%s\", 40);", name, name);
				allNames.add(field + "\n");
			}

		}
		logger.info("prodNames: " + allNames);
	}

	public void insertJSONIntoDB(JSONObject pObject) {
		JSONArray names = pObject.names();
		logger.info("names: " + names);
		JSONArray products = pObject.getJSONArray("products");
		int size = products.length();

		for (int i = 0; i < size; i++) {
			JSONObject obj = products.getJSONObject(i);
			JSONArray prodNames = obj.names();

			int prodSize = prodNames.length();
			for (int j = 0; j < prodSize; j++) {
				String name = prodNames.getString(j);
				logger.info(name + ":" + obj.getString(name));
				// allNames.add(name.toLowerCase() + " " + "VARCHAR(40)");
			}

		}
		logger.info("prodNames: " + allNames);
	}

	public String getIdType() {
		return mIdType;
	}

	public void setIdType(String pIdType) {
		mIdType = pIdType;
	}

	public static boolean isRequestsThrottled() {
		return mRequestsThrottled;
	}

	public static void setRequestsThrottled(boolean pRequestsThrottled) {
		mRequestsThrottled = pRequestsThrottled;
	}

}
