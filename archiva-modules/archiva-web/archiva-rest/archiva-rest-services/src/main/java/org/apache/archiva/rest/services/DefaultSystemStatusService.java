package org.apache.archiva.rest.services;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.archiva.repository.scanner.RepositoryScanner;
import org.apache.archiva.repository.scanner.RepositoryScannerInstance;
import org.apache.archiva.rest.api.model.CacheEntry;
import org.apache.archiva.rest.api.model.ConsumerScanningStatistics;
import org.apache.archiva.rest.api.model.QueueEntry;
import org.apache.archiva.rest.api.model.RepositoryScannerStatistics;
import org.apache.archiva.rest.api.services.ArchivaRestServiceException;
import org.apache.archiva.rest.api.services.SystemStatusService;
import org.codehaus.plexus.cache.Cache;
import org.codehaus.plexus.cache.CacheStatistics;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author Olivier Lamy
 * @since 1.4-M3
 */
@Service( "systemStatusService#rest" )
public class DefaultSystemStatusService
    extends AbstractRestService
    implements SystemStatusService
{


    private Map<String, TaskQueue> queues = null;

    private Map<String, Cache> caches = null;

    private RepositoryScanner scanner;


    @Inject
    public DefaultSystemStatusService( ApplicationContext applicationContext, RepositoryScanner scanner )
    {
        this.scanner = scanner;

        queues = getBeansOfType( applicationContext, TaskQueue.class );

        caches = getBeansOfType( applicationContext, Cache.class );
    }

    public String getMemoryStatus()
        throws ArchivaRestServiceException
    {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long total = runtime.totalMemory();
        long used = total - runtime.freeMemory();
        long max = runtime.maxMemory();
        return formatMemory( used ) + "/" + formatMemory( total ) + " (Max: " + formatMemory( max ) + ")";
    }

    private static String formatMemory( long l )
    {
        return l / ( 1024 * 1024 ) + "M";
    }

    public String getCurrentServerTime( String locale )
        throws ArchivaRestServiceException
    {
        SimpleDateFormat sdf = new SimpleDateFormat( "EEE, d MMM yyyy HH:mm:ss Z", new Locale( locale ) );
        return sdf.format( new Date() );
    }

    public List<QueueEntry> getQueueEntries()
        throws ArchivaRestServiceException
    {
        try
        {
            List<QueueEntry> queueEntries = new ArrayList<QueueEntry>( queues.size() );
            for ( Map.Entry<String, TaskQueue> entry : queues.entrySet() )
            {
                queueEntries.add( new QueueEntry( entry.getKey(), entry.getValue().getQueueSnapshot().size() ) );
            }

            return queueEntries;
        }
        catch ( TaskQueueException e )
        {
            log.error( e.getMessage(), e );
            throw new ArchivaRestServiceException( e.getMessage(),
                                                   Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() );
        }
    }

    public List<CacheEntry> getCacheEntries()
        throws ArchivaRestServiceException
    {
        List<CacheEntry> cacheEntries = new ArrayList<CacheEntry>( caches.size() );
        DecimalFormat decimalFormat = new DecimalFormat( "#%" );

        for ( Map.Entry<String, Cache> entry : caches.entrySet() )
        {
            CacheStatistics cacheStatistics = entry.getValue().getStatistics();
            cacheEntries.add( new CacheEntry( entry.getKey(), cacheStatistics.getSize(), cacheStatistics.getCacheHits(),
                                              cacheStatistics.getCacheMiss(),
                                              decimalFormat.format( cacheStatistics.getCacheHitRate() ).toString() ) );
        }

        return cacheEntries;
    }

    public Boolean clearCache( String cacheKey )
        throws ArchivaRestServiceException
    {
        Cache cache = caches.get( cacheKey );
        if ( cache == null )
        {
            throw new ArchivaRestServiceException( "no cache for key: " + cacheKey,
                                                   Response.Status.BAD_REQUEST.getStatusCode() );
        }

        cache.clear();
        return Boolean.TRUE;
    }

    public Boolean clearAllCaches()
        throws ArchivaRestServiceException
    {
        for ( Cache cache : caches.values() )
        {
            cache.clear();
        }
        return Boolean.TRUE;
    }

    public List<RepositoryScannerStatistics> getRepositoryScannerStatistics()
        throws ArchivaRestServiceException
    {
        Set<RepositoryScannerInstance> repositoryScannerInstances = scanner.getInProgressScans();
        if ( repositoryScannerInstances.isEmpty() )
        {
            return Collections.emptyList();
        }
        List<RepositoryScannerStatistics> repositoryScannerStatisticsList =
            new ArrayList<RepositoryScannerStatistics>( repositoryScannerInstances.size() );

        for ( RepositoryScannerInstance instance : repositoryScannerInstances )
        {
            RepositoryScannerStatistics repositoryScannerStatistics = new RepositoryScannerStatistics();
            repositoryScannerStatisticsList.add( repositoryScannerStatistics );
            repositoryScannerStatistics.setManagedRepository( instance.getRepository() );
            //repositoryScannerStatistics.setRepositoryScanStatistics( instance.getStatistics() );
            //repositoryScannerStatistics.setConsumerCounts( new HashMap<String, Long>( instance.getConsumerCounts() ) );
            //repositoryScannerStatistics.setConsumerTimings(
            //    new HashMap<String, Long>( instance.getConsumerTimings() ) );
            repositoryScannerStatistics.setNewFileCount( instance.getStats().getNewFileCount() );
            repositoryScannerStatistics.setTotalFileCount( instance.getStats().getTotalFileCount() );
            repositoryScannerStatistics.setConsumerScanningStatistics( mapConsumerScanningStatistics( instance ) );
        }

        return repositoryScannerStatisticsList;
    }

    private List<ConsumerScanningStatistics> mapConsumerScanningStatistics( RepositoryScannerInstance instance )
    {
        // FIXME take care of NPE here !!!
        List<ConsumerScanningStatistics> ret =
            new ArrayList<ConsumerScanningStatistics>( instance.getConsumerCounts().size() );
        for ( Map.Entry<String, Long> entry : instance.getConsumerCounts().entrySet() )
        {
            ConsumerScanningStatistics consumerScanningStatistics = new ConsumerScanningStatistics();
            consumerScanningStatistics.setConsumerKey( entry.getKey() );
            consumerScanningStatistics.setCount( entry.getValue() );
            consumerScanningStatistics.setTime( instance.getConsumerTimings().get( entry.getKey() ) );
            ret.add( consumerScanningStatistics );
        }
        return ret;
    }
}